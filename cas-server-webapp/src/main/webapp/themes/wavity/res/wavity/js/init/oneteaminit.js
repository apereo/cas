define(
	[
		'require', 
		'jquery', 
		'underscore', 
		'backbone', 
		'marionette', 
		'js/oneteam', 
		'js/routers/oneteamrouter', 
		'js/controllers/oneteamcontroller',
		'js/events/application/vent',
		'js/layouts/home/home',
		'js/caches/featuresCache',
		'js/caches/userDetailsCache',
		'js/caches/spaceCache',
		'js/caches/activitiesCache'
	], 
	function(
		require, 
		$, 
		_, 
		Backbone, 
		Marionette, 
		OneTeam, 
		Router, 
		Controller,
		Vent,
		Layout,
		Features,
		UserDetails,
		Space,
		Activities
	) {
		OneTeam.UserDetails = new UserDetails();
		OneTeam.Spaces = new Space();
		OneTeam.Features = new Features();
		OneTeam.Activities = new Activities();
		OneTeam.UserDetails = new UserDetails();
		OneTeam.Layout = new Layout();
		OneTeam.Controller = new Controller();
		OneTeam.ApplicationVent = new Vent();
		OneTeam.Router = new Router({ controller: OneTeam.Controller });		
		OneTeam.start();
		$(function() {
			console.log("1-TEAM application is ready.");
		});
});

// EOF