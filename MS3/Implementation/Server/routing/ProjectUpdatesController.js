/*jslint node:true nomen:true es5:true*/
"use strict";

/**
 * Router-Middleware für Projektupdates
 * @module routing/ProjectUpdatesController
 * @requires express
 * @requires util/dbam
 * @author Leonid Vilents <lvilents@smail.th-koeln.de>
 */
var express             = require('express');

var dbam                = require('../util/dbam.js');

var projectUpdatesController  = express.Router();


/**
 * @function
 * @name ProjectUpdatesController::createProjectUpdate
 * @desc Legt ein neues Projektupdate an
 * @param {string} path - Route
 * @param {callback} middleware - HTTP-Middleware mit Request- und Response-Objekt
 * @todo <strong>Implementieren</strong>
 */
projectUpdatesController.post('/', /*requireLogin,*/ function (req, res) {
    
});


/** Router-Handling für Projektupdates
 * @param {string} path - Route
 */
projectUpdatesController.route('/:id')
    /** @function
     * @name ProjectUpdateController::updateProjectUpdate
     * @desc Aktualisiert einen Datensatz zu einem Projektupdate
     * @param {callback} middleware - HTTP-Middleware mit Request- und Response-Objekt
     * @todo <strong>Implementieren</strong>
     */
    .put(function (req, res) {
    
    })
    /** @function
     * @name ProjectsController::deleteProjectUpdate
     * @desc Löscht ein Projektupdate
     * @param {callback} middleware - HTTP-Middleware mit Request- und Response-Objekt
     * @todo <strong>Implementieren</strong>
     */
    .delete(function (req, res) {
    
    });

module.exports = projectUpdatesController;