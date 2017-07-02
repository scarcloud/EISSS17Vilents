/*jslint node:true nomen:true*/
"use strict";

/** 
 * Hauptkommunikationsmodul für Datenbankzugriffe.
 * DBAM = "Data Base Access Module"
 * @module util/dbam
 * @requires node-mysql
 * @requires fs
 * @author Leonid Vilents <lvilents@smail.th-koeln.de>
 */

var mysql   = require('mysql');
var fs      = require('fs');
var pool    = null;


/** @todo für Produktivumgebung entfernen! */
console.log("[DBAM] DBAM module loaded.");

/**
 * Entnimmt der im System hinterlegten Konfigurationsdatei die Zugangsdaten für die Datenbank
 * @returns {object} JSON-Objekt mit Login-Credentials für die Datenbank
 */
function getCredentialsFromJson() {
    var content = fs.readFileSync('./util/config/dbam.json');
    return JSON.parse(content);
}


/**
 * Callback-Funktion für Einzelabfragen
 * 
 * @callback singleQueryCallback
 * @param {object} error - Fehler-Objekt, falls ein Fehler aufgetreten ist
 * @param {object} results - Ergebnisse der Abfrage
 */

/**
 * @function
 * @name DBAM::executeSingleQuery
 * @desc Führt eine einzelne Datenbankabfrage aus.
 * @param {string} sql - Der Query-String in SQL, möglicherweise mit Parametern
 * @param {array} values - Werte-Array für parametrisierbare Query-Strings
 * @param {singleQueryCallback} callback - Callbackfunktion zum Verarbeiten der Return-Wertes
 */
function executeSingleQuery(sql, values, callback) {
    pool.getConnection(function (connError, conn) {
        if (connError) {
            callback(connError, null);
        }
        console.log("Connected with ID " + conn.threadId);
        conn.query(
            sql,
            values,
            function (queryError, results, fields) {
                if (queryError) {
                    callback(queryError, null);
                } else {
                    console.log(results);
                    callback(null, results);
                }
            }
        );
    });
}

/**
 * @function
 * @name DBAM:Exports:initializeConnection
 * @desc Erstellt eine Verbindung zum Datenbankserver.
 * Wird einmalig bei Ausführung der Serverlogik ausgeführt.
 */
exports.initializeConnection = function () {
    this.pool = mysql.createPool(getCredentialsFromJson());
    console.log("[DBAM] Connection Pool created");
};


/**
 * Callback-Funktion für Registrierungsversuch
 * 
 * @callback trySignupCallback
 * @param {object} error - Fehler-Objekt, falls ein Fehler aufgetreten ist
 */

/**
 * @function
 * @name DBAM:Exports:trySignUp
 * @desc Versucht, einen User in der Datenbank anzulegen. Bei Erfolg, wird der Typ des Users in der Datenbank bestimmt, und 0 zurückgegeben.
 * @param {object} newUser - Ein newUser-Objekt, bestehend aus den Werten email, type, password und dateOfBirth
 * @param {trySignupCallback} callback - Callbackfunktion zum Verarbeiten der Return-Wertes
 * @throws Fehler bei MySQL
 */
exports.trySignup = function (newUser, callback) {
    console.log("[DBAM] trySignup");
    this.pool.getConnection(function (connectionError, conn) {
        if (connectionError) {
            callback(connectionError);
        }
        console.log("[DBAM] Connected with ID " + conn.threadId);
        
        conn.query({
            sql: "INSERT INTO benutzer (email, passwort, geburtsdatum) VALUES(?, ?, ?)",
            values: [newUser.email, newUser.password, newUser.dateOfBirth]
        }, function (insertError, results, fields) {
            if (insertError) {
                callback(insertError);
            }
            console.log("[DBAM] Insert 1 successful");
            var sql2 = "INSERT INTO ";
            if (newUser.type === "casemodder") {
                sql2 += "casemodder (user_id) VALUES (?)";
            } else {
                sql2 += "sponsor (user_id) VALUES (?)";
            }
            conn.query(sql2, [results.insertId], function (insertError2, results2, fields2) {
                conn.release();
                if (insertError2) {
                    callback(insertError2);
                }
                console.log("[DBAM] Insert 2 successful");
                callback(null);
            });
        });
    });
};


/**
 * Callbackfunktion für Suche via Email
 * 
 * @callback findUserByEmailCallback
 * @param {object} error - Fehler-Objekt, falls ein Fehler aufgetreten ist
 * @param {object} results - Ergebnis aus Abfrage
 */

/**
 * Sucht in der Datenbank einen Benutzer nach Email-Adresse.
 * @param {string} email - Email-Adresse des Benutzers
 * @param {findUserByEmailCallback} callback - Callbackfunktion zum Verarbeiten der Rückgabewerte
 * @returns {object} JSON-Objekt mit öffentlichen Benutzerdaten
 */
exports.findUserByEmail = function (email, callback) {
    console.log("[DBAM] findUserByEmail");
    this.pool.getConnection(function (connectionError, conn) {
        if (connectionError) {
            callback(connectionError, null);
        }
        console.log("[DBAM] Connected with ID " + conn.threadId);
        conn.query('SELECT * FROM benutzer WHERE email = ? LIMIT 1', [email], function (selectError, results, fields) {
            if (selectError) {
                conn.release();
                callback(selectError, null);
            }
            if (results) {
                conn.query('SELECT * FROM casemodder WHERE user_id = ? LIMIT 1', [results[0].id], function (typeSelectError, typeResults, typeFields) {
                    conn.release();
                    if (typeSelectError) {
                        callback(typeSelectError, null);
                    }
                    if (typeResults) {
                        results[0].type = "casemodder";
                    } else {
                        results[0].type = "sponsor";
                    }
                    callback(null, results);
                });
            } else {
                callback(null, null);
            }
        });
    });
};


/**
 * Callbackfunktion für das Prüfen von neuen Nachrichten
 * 
 * @callback checkForNewMessagesCallback
 * @param {object} error - Fehler-Objekt, falls ein Fehler aufgetreten ist
 * @param {int} count - Anzahl von ungelesenen Nachrichten
 */

/**
 * Sucht nach ungelesenen Nachrichten im Postfach des Benutzers
 * @param {int} userId - ID des Benutzers
 * @param {checkForNewMessagesCallback} callback - Callbackfunktion zum Verarbeiten der Rückgabewerte
 */
exports.checkForNewMessages = function (userId, callback) {
    console.log("[DBAM] checkForNewMessages");
    this.pool.getConnection(function (connError, conn) {
        if (connError) {
            callback(connError, null);
        }
        console.log("Connected with ID " + conn.threadId);
        conn.query(
            "SELECT COUNT(*) FROM nachricht WHERE empfanger_id = ? AND gelesen = 0",
            [userId],
            function (queryError, results, fields) {
                if (queryError) {
                    callback(queryError, null);
                } else {
                    console.log(results);
                    callback(null, results);
                }
            }
        );
    });
};


/**
 * Callbackfunktion für Zählung von Projekten und Updates
 * 
 * @callback countProjectsCallback
 * @param {object} error - Fehler-Objekt, falls ein Fehler aufgetreten ist
 * @param {object} results - Ergebnis aus Abfrage
 */

/**
 * Zählt die Projekte und Projektupdates eines Benutzers
 * @param {int} userId - Die ID des Benutzers
 * @param {countProjectsCallback} callback - Callbackfunktion zum Verarbeiten der Rückgabewerte
 */
exports.countUserProjects = function (userId, callback) {
    console.log("[DBAM] countUserProjects");
    var resObj = {
        projects: 0,
        projectUpvotes: 0,
        projectUpdates: 0,
        projectUpdateUpvotes: 0
    };
    this.pool.getConnection(function (connectionError, conn) {
        if (connectionError) {
            callback(connectionError, null);
        }
        console.log("[DBAM] Connected with ID " + conn.threadId);
        conn.query('SELECT * FROM projekt WHERE casemodder_id = ?', [userId], function (selectError, results, fields) {
            if (selectError) {
                conn.release();
                callback(selectError, null);
                return;
            }
            if (results.length > 0) {
                var values = [],
                    i = 0,
                    len = results.length;
                for (i, len; i < len; i += 1) {
                    values[i] = results[i].id;
                }
                
                resObj.projects = len;
                
                conn.query('SELECT COUNT(*) FROM projekt_upvote WHERE projekt_id IN ?', [conn.escape(values)], function (pUpvoteError, pUpvoteResults, pUpvoteFields) {
                    if (pUpvoteError) {
                        callback(pUpvoteError, null);
                        return;
                    }
                    if (results) {
                        resObj.projectUpvotes = results[0];
                    }
                });
                
                conn.query('SELECT COUNT(*) FROM projektupdate WHERE projekt_id IN ?', [conn.escape(values)], function (projectUpdateError, puResults, puFields) {
                    if (projectUpdateError) {
                        conn.release();
                        callback(projectUpdateError, null);
                        return;
                    }
                    if (puResults) {
                        resObj.projectUpdates = puResults[0];
                        
                        var puValues = [],
                            j = 0,
                            puLen = puResults.length;
                        for (j, puLen; j < puLen; j += 1) {
                            values[j] = puResults[j].id;
                        }
                        
                        conn.query('SELECT COUNT(*) FROM projektupdate_upvote WHERE projektupdate_id IN ?', [conn.escape(puValues)], function (puUpvoteError, puUpvoteResults, puUpvoteFields) {
                            conn.release();
                            if (puUpvoteError) {
                                callback(puUpvoteError, null);
                            }
                            resObj.projectUpdateUpvotes = puUpvoteResults[0];
                            callback(null, resObj);
                        });
                        
                    } else {
                        callback(null, resObj);
                    }
                });
            } else {
                conn.release();
                callback(null, resObj);
            }
        });
    });
};


/**
 * Callbackfunktion für Zählung von Benutzerkommentaren
 * 
 * @callback countCommentsCallback
 * @param {object} error - Fehler-Objekt, falls ein Fehler aufgetreten ist
 * @param {int} count - Ergebnis aus Abfrage
 */

/**
 * Zählt die Kommentare eines Benutzers
 * @param {int} userId - Die ID des Benutzers
 * @param {countCommentsCallback} callback - Callbackfunktion zum Verarbeiten der Rückgabewerte
 * @todo <strong>Implementieren</strong>
 */
exports.countUserComments = function (userId, callback) {
    var resObj = {
        comments: 0,
        commentUpvotes: 0
    };
    console.log("[DBAM] countUserComments");
    this.pool.getConnection(function (connectionError, conn) {
        if (connectionError) {
            callback(connectionError, null);
            
        }
        console.log("[DBAM] Connected with ID " + conn.threadId);
        conn.query('SELECT * FROM kommentar WHERE benutzer_id = ?', [userId], function (selectError, results, fields) {
            if (selectError) {
                conn.release();
                callback(selectError, null);
                return;
            }
            if (results.length > 0) {
                resObj.comments = results.length;
                var values = [],
                    i = 0,
                    len = results.length;
                for (i, len; i < len; i += 1) {
                    values[i] = results[i].id;
                }
                
                conn.query('SELECT COUNT(*) FROM kommentar_upvote WHERE kommentar_id IN ?', [conn.escape(values)], function (upvoteError, upvoteResults, upvoteFields) {
                    conn.release();
                    if (upvoteError) {
                        callback(upvoteError, null);
                    }
                    if (upvoteResults) {
                        resObj.commentUpvotes = upvoteResults[0];
                    }
                    callback(null, resObj);
                });
            } else {
                conn.release();
                callback(null, resObj);
            }
        });
    });
};

module.exports = exports;