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

var mysql       = require('mysql');
var fs          = require('fs');
var fileManager = require('./filemanager.js');
var pool        = null;


/** @todo für Produktivumgebung entfernen! */
console.log("[DBAM] DBAM module loaded.");

/**
 * Entnimmt der im System hinterlegten Konfigurationsdatei die Zugangsdaten für die Datenbank
 * @returns {object} JSON-Objekt mit Login-Credentials für die Datenbank
 */
function getCredentialsFromJson() {
  var content = fs.readFileSync('./util/config/dbam.json');
  console.log('[DBAM] Credentials received');
  return JSON.parse(content);
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
 * Hilfsfunktion zur Erstellung von inneren Queries bei der Projektabfrage
 * für Sponsoren
 * @param   {string} index Teampositionsnummer
 * @returns {string} das fertige Subquery
 */
var sponsorTeamSubquery = function (index) {
  return "(SELECT casemodder" + index + "_id FROM team WHERE sponsor_id = ?)";
};

/**
 * Callback-Funktion für INSERT-Abfragen
 * 
 * @callback insertCallback
 * @param {object} error - Fehler-Objekt, falls ein Fehler aufgetreten ist
 */

/**
 * Callback-Funktion für SELECT-Abfragen
 * 
 * @callback selectCallback
 * @param {object} error - Fehler-Objekt, falls ein Fehler aufgetreten ist
 * @param {object} results - Ergebnisse aus Abfrage
 */

/**
 * Holt Kommentare für ein Spezifisches Element.
 * @param {object}         conn      DB-Verbindung
 * @param {int}            id        Projekt-ID
 * @param {boolean}        isProject Flag für Elementtyp
 * @param {selectCallback} callback  Callbackfunktion
 */
function getComments(conn, id, isProject, callback) {
    
    var countSql = "(SELECT COUNT(*) FROM kommentar_upvote ku WHERE ku.kommentar_id = k.id) AS upvoteCount",
        joinSql = "JOIN benutzer b ON b.id = k.benutzer_id ",
        sql = "SELECT b.vorname, b.nachname, k.*, " + countSql + " FROM kommentar k " + joinSql;
    
    if (isProject) {
        sql += "JOIN projekt_kommentar pk ON k.id = pk.kommentar_id WHERE pj.projekt_id = ?";
    } else {
        sql += "JOIN projektupdate_kommentar puk ON k.id = puk.kommentar_id WHERE puk.projekt_id = ?";
    }
    conn.query(sql, [id], function (error, results, fields) {
        if (error) {
            callback(error, null);
            return;
        }
        callback(null, JSON.parse(JSON.stringify(results)));
        return;
    });
}

/**
 * @function
 * @name DBAM:Exports:trySignUp
 * @desc Versucht, einen User in der Datenbank anzulegen. Bei Erfolg, wird der Typ des Users in der Datenbank bestimmt.
 * @param {object} newUser - Ein newUser-Objekt, bestehend aus den Werten email, type, password und dateOfBirth
 * @param {insertCallback} callback - Callbackfunktion zum Verarbeiten der Return-Wertes
 * @throws Fehler bei MySQL
 */
exports.trySignup = function trySignup(newUser, callback) {
  console.log("[DBAM] trySignup");
  this.pool.getConnection(function (connectionError, conn) {
    if (connectionError) {
      callback(connectionError);
      return;
    }
    console.log("[DBAM] Connected with ID " + conn.threadId);
        
    conn.beginTransaction(function (transactionError) {
      if (transactionError) {
        callback(transactionError);
        return;
      }

      conn.query({
        sql: "INSERT INTO benutzer (email, passwort, geburtsdatum) VALUES(?, ?, ?)",
        values: [newUser.email, newUser.password, newUser.dateOfBirth]
      }, function (error, results, fields) {
        if (error) {
          return conn.rollback(function () {
            conn.release();
            callback(error);
            return;
          });
        }
        console.log("[DBAM] Insert 1 successful");
                
        conn.query({
          sql: "INSERT INTO ?? (user_id) VALUES (?)",
          values: [newUser.type, results.insertId]
        }, function (typeError, typeResults, typeFields) {
          if (typeError) {
            return conn.rollback(function () {
              conn.release();
              callback(typeError);
              return;
            });
          }
          console.log("[DBAM] Insert 2 successful");
            
          conn.commit(function (commitError) {
            conn.release();
            if (commitError) {
              return conn.rollback(function () {
                callback(commitError);
                return;
              });
            }
            console.log("[DBAM] Commit succesful");
                        
            if (newUser.type === "sponsor" && newUser.accreditFile) {
              fileManager.handleAccreditFileUpload(
                newUser.email,
                results.insertId,
                newUser.accreditFile,
                function (fileUploadError) {
                  if (fileUploadError) {
                    callback(fileUploadError);
                    return;
                  } else {
                    callback(null);
                    return;
                  }
                }
              );
            }
          });
        });
      });
    });
  });
};

/**
 * Fügt der Datenbank Referenzen zu einer Datei hinzu.
 * @param   {object}   options  Optionen-Objekt. Enthält eine Relationsreferenz, den Dateipfad und den Dateityp.
 * @param   {insertCallback} callback Callbackfunktion
 */
exports.insertFile = function insertFile(options, callback) {
    
  var invalidOptions = (!options.relation || !options.path || !options.type);
  if (invalidOptions) {
    callback(new Error("[DBAM] Invalid options object"));
    return;
  }
    
  this.pool.getConnection(function (connError, conn) {
    if (connError) {
      callback(connError);
      return;
    }
        
    conn.beginTransaction(function (transactionError) {
      if (transactionError) {
        conn.release();
        callback(transactionError);
        return;
      }
            
      conn.query({
        sql: "INSERT INTO datei (pfad, dateityp) VALUES(?, ?);",
        values: [options.path, options.type]
      }, function (error, results, fields) {
        if (error) {
          return conn.rollback(function () {
            conn.release();
            callback(error);
            return;
          });
        }

        var validRelationOptions = (
          options.relation.user || options.relation.project || options.relation.projectupdate
        ),
            values = [];
        if (!validRelationOptions) {
          return conn.rollback(function () {
            conn.release();
            callback(new Error("Invalid Relation Option"));
            return;
          });
        }
                
        //SQL-Parameter setzen
        if (options.relation.user) {
          values = [
            "datei_benutzer",
            "benutzer_id",
            options.relation.user,
            results.insertId
          ];
        } else if (options.relation.project) {
          values = [
            "datei_projekt",
            "projekt_id",
            options.relation.project,
            results.insertId
          ];
        } else if (options.relation.projectupdate) {
          values = [
            "datei_projektupdate",
            "projektupdate_id",
            options.relation.projectupdate,
            results.insertId
          ];
        }
                
        conn.query({
          sql: "INSERT INTO ?? (??, datei_id) VALUES (?, ?)",
          values: values
        }, function (relationError, relationResults, relationFields) {

          if (relationError) {
            return conn.rollback(function () {
              conn.release();
              callback(relationError);
              return;
            });
          }

          conn.commit(function (commitError) {
            conn.release();
            if (commitError) {
              return conn.rollback(function () {
                callback(commitError);
                return;
              });
            } else {
              callback(null);
              return;
            }
          });
        });
      });
    });
  });
};

/**
 * Sucht in der Datenbank einen Benutzer nach Email-Adresse.
 * @param {string} email - Email-Adresse des Benutzers
 * @param {selectCallback} callback - Callbackfunktion zum Verarbeiten der Rückgabewerte
 */
exports.findUserByEmail = function findUserByEmail(email, callback) {

  this.pool.getConnection(function (connectionError, conn) {

    if (connectionError) {
      callback(connectionError, null);
    }

    conn.query(
      'SELECT * FROM benutzer WHERE email = ? LIMIT 1',
      [email],
      function (selectError, results, fields) {

        if (selectError) {
          conn.release();
          callback(selectError, null);
          return;
        }

        if (results.length === 1) {

          conn.query(
            'SELECT * FROM casemodder WHERE user_id = ? LIMIT 1',
            [results[0].id],
            function (typeSelectError, isCasemodderResults, typeFields) {
            
              conn.release();
              if (typeSelectError) {
                callback(typeSelectError, null);
                return;
              }

              results[0].isCasemodder = isCasemodderResults.length > 0 ? true : false;
              callback(null, JSON.stringify(results[0]));
              return;
            }
          );
        } else {
          callback(null, null);
          return;
        }
      }
    );
  });
};

/**
 * Ruft die Profildaten eines Benutzers ab
 * @param {int} userId - ID des Benutzers
 * @param {string} userType - Benutzertyp
 * @param {selectCallback} callback - Callbackfunktion zum Verarbeiten der Rückgabewerte
 */
exports.getProfileData = function getProfileData(userId, userType, callback) {
  this.pool.getConnection(
    function (connError, conn) {
      if (connError) {
        callback(connError, null);
        return;
      }
      var isCasemodder = (userType === "casemodder"),
          returnData = isCasemodder
          ? {
            nachname: "",
            vorname: "",
            suchstatus: false,
            wohnort: "",
            beschreibung: ""
          }
          : {
            nachname: "",
            vorname: "",
            firma: "",
            beschreibung: ""
          };
      conn.query(
        "SELECT * FROM benutzer WHERE id = ? LIMIT 1",
        [userId],
        function (userError, userResults, userFields) {
          if (userError) {
            conn.release();
            callback(userError, null);
            return;
          }
          if (userResults.length > 0) {
            returnData.nachname = userResults[0].nachname;
            returnData.vorname = userResults[0].vorname;
            conn.query(
              "SELECT * FROM ?? WHERE user_id = ?",
              [userType, userId],
              function (typeError, typeResults, typeFields) {
                conn.release();
                if (typeError) {
                  callback(typeError, null);
                  return;
                }
                if (typeResults.length > 0) {
                  switch (userType) {
                  case "casemodder":
                    returnData.suchstatus = typeResults[0].suchstatus;
                    returnData.wohnort = typeResults[0].wohnort;
                    returnData.beschreibung = typeResults[0].beschreibung;
                    break;
                  case "sponsor":
                    returnData.firma = typeResults[0].firma;
                    returnData.beschreibung = typeResults[0].beschreibung;
                    break;
                  default:
                    break;
                  }
                  callback(null, returnData);
                  return;
                }
              }
            );
          }
        }
      );
    }
  );
};

/**
 * Holt "Header-Informationen" über die Projekte eines Benutzers (ID und Titel)
 * @param {int} userId   BenutzerID
 * @param {selectCallback} callback Callbackfunktion
 */
exports.getProjectsForUser = function getProjectsForUser(userId, callback) {
  this.pool.getConnection(function (connError, conn) {
    if (connError) {
      callback(connError, null);
      return;
    }
    conn.query(
      'SELECT id, titel FROM projekt WHERE casemodder_id = ? ORDER BY erstellt_am DESC',
      [userId],
      function (error, results, fields) {
        conn.release();
        if (error) {
          callback(error, null);
          return;
        }
        results = JSON.parse(JSON.stringify(results));
        callback(null, results);
        return;
      }
    );
  });
};

/**
 * Sucht nach ungelesenen Nachrichten im Postfach des Benutzers
 * @param {int} userId - ID des Benutzers
 * @param {selectCallback} callback - Callbackfunktion zum Verarbeiten der Rückgabewerte
 */
exports.checkForNewMessages = function checkForNewMessages(userId, callback) {
  this.pool.getConnection(function (err, conn) {
    if (err) {
      callback(err, null);
    }
    console.log("[DBAM] checkNewMessages: Connected with ID " + conn.threadId);
    conn.query(
      'SELECT COUNT(*) AS newMessages FROM nachricht WHERE empfanger_id = ? AND ungelesen = 1',
      [userId],
      function (err, row, fields) {
        if (err) {
          callback(queryError, null);
          return;
        } else {
          console.log(row[0]);
          callback(null, row[0].newMessages);
          return;
        }
      }
    );
  });
};

/**
 * Holt seitengerecht jeweils 8 Projekte aus der Datenbank,
 * sortiert absteigend nach Erstellungsdatum
 * @param {int} page - Setnummer
 * @param {selectCallback} callback - Callbackfunktion zum Verarbeiten der Rückgabewerte
 */
exports.getProjectsOverviewData = function (userId, isCasemodder, callback) {
    console.log("[DBAM] getLatestProjects");
    this.pool.getConnection(function (connError, conn) {
        if (connError) {
            callback(connError, null);
            return;
        }
        console.log("[DBAM] getLatestProjects: Connected with ID " + conn.threadId);
        // SQL für Sponsor-Abfragen zu Gunsten von JSLint bereits hier eingebungen.
        var resObj = {
                latestProjects: null
            },
            casemodder1_substr = sponsorTeamSubquery("1"),
            casemodder2_substr = sponsorTeamSubquery("2"),
            casemodder3_substr = sponsorTeamSubquery("3"),
            latestProjectsSQL = "SELECT * FROM projekt WHERE casemodder_id NOT IN (" + casemodder1_substr + ", " + casemodder2_substr + ", " + casemodder3_substr + ") ORDER BY erstellt_am DESC",
            teamProjectsSQL = "SELECT * FROM projekt WHERE casemodder_id IN (" + casemodder1_substr + ", " + casemodder2_substr + ", " + casemodder3_substr + ")";
        if (isCasemodder) {
            conn.query(
                "SELECT * FROM projekt WHERE casemodder_id != ? ORDER BY erstellt_am DESC",
                [userId],
                function (latestError, latestResults, latestFields) {
                    if (latestError) {
                        conn.release();
                        callback(latestError, null);
                        return;
                    }
                    resObj.latestProjects = JSON.parse(JSON.stringify(latestResults));
                }
            );
            conn.query(
                "SELECT * FROM projekt WHERE casemodder_id = ?",
                [userId],
                function (ownedError, ownedResults, ownedFields) {
                    conn.release();
                    if (ownedError) {
                        callback(ownedError, null);
                        return;
                    }
                    resObj.ownedProjects = JSON.parse(JSON.stringify(ownedResults));
                    callback(null, resObj);
                    return;
                }
            );
        } else {
            console.log("no casemodder");
            conn.query(
                latestProjectsSQL,
                [userId, userId, userId],
                function (latestError, latestResults, latestFields) {
                    if (latestError) {
                        conn.release();
                        callback(latestError, null);
                        return;
                    }
                    resObj.latestProjects = latestResults;
                }
            );
            conn.query(
                teamProjectsSQL,
                [userId, userId, userId],
                function (teamError, teamResults, teamFields) {
                    conn.release();
                    if (teamError) {
                        callback(teamError, null);
                        return;
                    }
                    resObj.teamProjects = teamResults;
                    callback(null, resObj);
                    return;
                }
            );
        }
    });
};

/**
 * Ruft alle Nachrichten ab, bei denen der Benutzer der Empfänger ist
 * @param {int} userId - Die ID des Benutzers
 * @param {selectCallback} callback - Callbackfunktion zum Verarbeiten der Rückgabewerte
 */
exports.getMessagesOverviewData = function (userId, callback) {
    console.log('[DBAM] getMessages');
    this.pool.getConnection(function (connError, conn) {
        if (connError) {
            callback(connError, null);
            return;
        }
        console.log('[DBAM] getMessages: Connected with ID ' + conn.threadId);
        conn.query('SELECT n.id as n_id, n.zeitstempel, n.ungelesen, b.id as b_id, b.vorname, b.nachname FROM nachricht n JOIN benutzer b ON n.absender_id = b.id WHERE empfanger_id = ? ORDER BY n.zeitstempel DESC', [userId], function (error, results, fields) {
            conn.release();
            if (error) {
                callback(error, null);
                return;
            }
            var data = {
                messages: JSON.parse(JSON.stringify(results))
            };
            console.log(data);
            callback(null, data);
            return;
        });
    });
};

/**
 * Holt alle Casemodder-Benutzer mit aktiviertem Suchstatus
 * @param {selectCallback} callback Callbackfunktion
 */
exports.getSponsoringApplicants = function (callback) {
    console.log("[DBAM] getSponsoringApplicatns");
    this.pool.getConnection(function (connError, conn) {
        if (connError) {
            callback(connError, null);
            return;
        }
        console.log("[DBAM] getSponsoringApplicants: Connected with ID " + conn.threadId);
        
        conn.query(
            "SELECT b.id, b.vorname, b.nachname FROM benutzer b JOIN casemodder c ON b.id = c.user_id WHERE c.suchstatus = 1",
            function (error, results, fields) {
                conn.release();
                if (error) {
                    callback(error, null);
                    return;
                }
                var resultsObj = JSON.parse(JSON.stringify(results));
                callback(null, resultsObj);
                return;
            }
        );
    });
};

/**
 * Zählt die Projekte und Projektupdates eines Benutzers
 * @param {int} userId - Die ID des Benutzers
 * @param {selectCallback} callback - Callbackfunktion zum Verarbeiten der Rückgabewerte
 */
exports.countUserProjects = function countUserProjects(userId, callback) {
    var resObj = {
        projects: 0,
        projectUpvotes: 0,
        projectUpdates: 0,
        projectUpdateUpvotes: 0
    };
    this.pool.getConnection(function (connectionError, conn) {
        if (connectionError) {
            callback(connectionError, null);
            return;
        }
        
        // Alle Projekte des Casemodders
        conn.query('SELECT * FROM projekt WHERE casemodder_id = ?', [userId], function (projectsError, projectsResults, projectsFields) {
            // Fehler abfangen
            if (projectsError) {
                conn.release();
                callback(projectsError, null);
                return;
            }
            // Gibt es Projekte vom Casemodder?
            if (projectsResults.length > 0) {
                
                var i = 0,
                    projectsCount = projectsResults.length,
                    projectUpvoteCountSQL = "SELECT COUNT(*) as count FROM projekt_upvote WHERE projekt_id IN (";
                
                // Anzahl Projekte setzen
                resObj.projects = projectsCount;
                
                // SQL-Sctring konstruieren
                for (i, projectsCount; i < projectsCount; i += 1) {
                    projectUpvoteCountSQL += projectsResults[i].id.toString() + (i === projectsCount - 1 ? "" : ", ");
                }
                projectUpvoteCountSQL += ")";
                
                // Alle Upvotes für die Projekte des Casemodders
                conn.query(projectUpvoteCountSQL, function (pUpvoteCountError, pUpvoteCountResults, pUpvoteCountFields) {
                    conn.release();
                    if (pUpvoteCountError) {
                        callback(pUpvoteCountError, null);
                        return;
                    }
                    // Gibt es Upvotes für die Projekte?
                    if (pUpvoteCountResults) {
                        resObj.projectUpvotes = pUpvoteCountResults[0].count;
                    }
                    
                    exports.countUserProjectUpdates(resObj, projectsResults, function (error, resultObject) {
                        if (error) {
                            callback(error, null);
                            return;
                        }
                        callback(null, resultObject);
                    });
                });   
            } else {
                conn.release();
                callback(null, resObj);
                return;
            }
        });
    });
};


/**
 * Zählt für ein Projekt die Projektupdates und alle dazugehörigen Upvotes
 * @param {object}   resultObject  Übergebenes Objekt mit Gesamtergebnissen
 * @param {object[]} projectsArray Array mit Projekt-Objekten
 * @param {selectCallback} callback Callbackfunktion
 */
exports.countUserProjectUpdates = function countUserProjectUpdates(resultObject, projectsArray, callback) {
    this.pool.getConnection(function (error, conn) {
        if (error) {
            callback(error, null);
        }
        
        if (projectsArray.length > 0) {
            var i = 0,
                projectsCount = projectsArray.length,
                projectUpdatesSQL = "SELECT * FROM projektupdate WHERE projekt_id IN (";
            for (i, projectsCount; i < projectsCount; i += 1) {
                projectUpdatesSQL += projectsArray[i].id.toString() + (i === projectsCount - 1 ? "" : ", ");
            }
            projectUpdatesSQL += ")";
            
            conn.query(
                projectUpdatesSQL,
                function (projectUpdatesError, projectUpdatesResults, projectUpdatesFields) {
                // Fehler abfangen
                if (projectUpdatesError) {
                    conn.release();
                    callback(projectUpdatesError, null);
                    return;
                }
                
                // Gibt es Projektupdates zu den Projekten?
                if (projectUpdatesResults.length > 0) {
                        
                    resultObject.projectUpdates = projectUpdatesResults.length;
                        
                    var j = 0,
                        puLen = projectUpdatesResults.length,
                        projectUpdatesUpvoteCountSQL = "SELECT COUNT(*) as count FROM projektupdate_upvote WHERE projektupdate_id IN (";
                    for (j, puLen; j < puLen; j += 1) {
                        projectUpdatesUpvoteCountSQL += projectUpdatesResults[j].id.toString() + (j === puLen - 1 ? "" : ", ");
                    }
                    projectUpdatesUpvoteCountSQL += ")";
                        
                    // Alle Upvotes für die Projektupdates der Projekte des Casemodders
                    conn.query(projectUpdatesUpvoteCountSQL, function (puUpvoteError, puUpvoteResults, puUpvoteFields) {
                        // Letzte Abfrage
                        conn.release();
                            
                        // Fehler abfangen
                        if (puUpvoteError) {
                            callback(puUpvoteError, null);
                            return;
                        }
                            
                        resultObject.projectUpdateUpvotes = puUpvoteResults[0].count;
                        callback(null, resultObject);
                        return;
                    });
                } else {
                    callback(null, resultObject);
                    return;
                }    
            });
        }
    })
};


/**
 * Zählt die Kommentare eines Benutzers
 * @param {int} userId - Die ID des Benutzers
 * @param {selectCallback} callback - Callbackfunktion zum Verarbeiten der Rückgabewerte
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
            return;
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
                        return;
                    }
                    if (upvoteResults) {
                        resObj.commentUpvotes = upvoteResults[0];
                    }
                    callback(null, resObj);
                    return;
                });
            } else {
                conn.release();
                callback(null, resObj);
                return;
            }
        });
    });
};



/**
 * Holt ein Projekt, seine Kommentare, Updates und alle dazugehörigen Upvotes.
 * @param {int} pId - ID des Projektes
 * @param {int} uId - ID des Session-Benutzers
 * @param {selectCallback} callback - Callbackfunktion
 */
exports.getProject = function (pId, uId, callback) {
  this.pool.getConnection(function (connError, conn) {
    if (connError) {
      callback(connError, null);
    }
    console.log('[DBAM] getProject: Connected with ID ' + conn.threadId);
    var countSql = '(SELECT COUNT(*) FROM projekt_upvote pu WHERE pu.projekt_id = ?) AS upvotes',
        sql = 'SELECT p.*, b.vorname, b.nachname, ' + countSql + ' FROM projekt p JOIN benutzer b ON p.casemodder_id = b.id WHERE p.id = ? LIMIT 1';
        
    conn.query(sql, [pId, pId], function (error, results, fields) {
      if (error) {
        callback(error, null);
        return;
      }
      if (results.length === 1) {
        var result = JSON.parse(JSON.stringify(results[0]));
        if (result.casemodder_id === uId) {
          result.userOwnsProject = true;
        } else {
          result.userOwnsProject = false;
        }
        console.log(result);
                
        // Get Comments for the Project
        conn.query(
          'SELECT id FROM kommentar k JOIN projekt_kommentar pk on k.id = pk.kommentar_id WHERE pk.projekt_id = ?',
          [pId],
          function (commentsError, commentsResults, commentsFields) {
                
            if (commentsError) {
              callback(commentsError, null);
              return;
            }
            commentsResults = JSON.parse(JSON.stringify(commentsResults));

            // Get project updates
            conn.query(
              'SELECT * FROM projektupdate WHERE projekt_id = ?',
              [pId],
              function (puError, puResults, puFields) {

                if (puError) {
                  conn.release();
                  callback(puError, null);
                  return;
                }

                result.updates = JSON.parse(JSON.stringify(puResults));
                console.log(result);
                callback(null, result);
              }
            );
          }
        );
      } else {
        callback(null, null);
        return;
      }
    });
  });
};


/**
 * Aktiviert den Suchstatus eines Casemodders
 * @param {int} userId   Benutzer-ID des Casemodders
 * @param {insertCallback} callback Callbackfunktion
 */
exports.activateSeekerStatus = function activateSeekerStatus(userId, callback) {
  this.pool.getConnection(
    function (connError, conn) {
      if (error) {
        callback(error);
      }
      conn.query(
        'UPDATE casemodder SET suchstatus = 1 WHERE user_id = ?',
        [userId],
        function (error, results, fields) {
          if (error) {
            callback(error);
            return;
          }
          callback(null);    
        }
      );
    }
  );
};


/**
 * Erstellt eine neue Nachricht in der Datenbank
 * @param {object} messageObject Das Nachrichtenobjekt
 * @param {insertCallback} callback Callbackfunktion
 */
exports.createNewMessage = function createNewMessage(messageObject, callback) {
  var invalidMessageObject = (messageObject.senderId === null || messageObject.reveiverId === null || messageObject.content === null);
  if (invalidMessageObject) {
    callback(new Error("Invalid Message Object"));
    return;
  }
  this.pool.getConnection(
    function (connError, conn) {
      if (connError) {
        callback(connError);
        return;
      }
      conn.query(
        "INSERT INTO nachricht (absender_id, empfanger_id, inhalt) VALUES (?, ?, ?)",
        [messageObject.senderId, messageObject.receiverId, messageObject.content],
        function (error, results, fields) {
          conn.release();
          if (error) {
            callback(error);
            return
          } else {
            callback(null);
          }
        }
      );
    }
  );
};




exports.deleteUserAccount = function deleteUserAccount(userId, callback) {
    
};


module.exports = exports;