##Prerequisite
1. Require msf library to communicate with mobile.
2. Require a  jquery & bootstrap library to make simple UI
3. Reference Site
 1. [MSF TV API Documentaion ](http://www.samsungdforum.com/TizenGuide/tizen3751/index.html): 
 2. [YouTube IFrame Player API](https://developers.google.com/youtube/player_parameters)

 

 

##How to install and run app to the Tizen TV
1. [Using Tizen TV SDK](http://www.samsungdforum.com/TizenGuide/tizen3511/index.html):  see this page

 
2. Using USB Memory
 1. Make folder "userwidget" in USB Memory.
 2. Move a app file '*.wgt' into "userwidget" folder.
 3. Connect USB memory to the TV and then TV install automatically.


 

##WebApp UI

Tizen Web App - index.html

	<link rel="stylesheet" href="./css/bootstrap.min.css">
	<script src="./js/jquery.min.js"></script>
	<script src="./js/bootstrap.min.js"></script>
	<script src="./js/msf.min.js"></script>
	<script src="./js/main.js"></script>
 
Use bootstrap(container/jumbotron) and 'iframe' tag to play youtube video.

	Tizen Web App - index.html
	
	 
	<div class="container" >
	  <div class="jumbotron">
	    <h1>MSF Youtube player</h1>
	    <p> play movie trailer</p>
	    <p id="title"> Title </p>
	  </div>
	
	  <div class="row" id="player" style="display:none">
	    <div class="col-sm-4">
	      <h3>thumnail</h3>
			<img id="thumnail" src="" width="380" height="300">
			</img>
	    </div>
	    <div class="col-sm-8">
	      <h3>player</h3>
			<iframe id="ytplayer" enablejsapi="1" type="text/html" width="740" height="440" src="" frameborder="5"/>
	    </div>


##Using Smart View SDK
 
1. Get a reference to the "local" service
2. Connect to a communication "channel"
3. Add Reserved Event Listeners - 'connect', 'disconnect', 'clientConnect','clientDisconnect'


		Tizen WebApp - main.js


		msf.local(function(err, service){
		    console.log('Start MSF');
		    var channel = service.channel(mChannel);
		    
		    console.log('Start MSF Channel = '+ mChannel);
		    
		    channel.connect({name: 'TV'}, function (err) {
		        if(err) {
		        	return console.log(err);
		        }
		        console.log('channel.connect');
		    });
		
		    channel.on('connect', function(client){
		    	console.log("connect client.id :"+client.id);
	
		    	if(channel.clients.length > 1){
					hideDiv("connection");
					displayDiv("info");
		    	}
		    });
		    
		    channel.on('disconnect', function(client){
		    	console.log("disconnect ");
		    });
		    
		    channel.on('clientConnect', function(client){
		    	console.log("clientConnect ");
	
		    	if(channel.clients.length > 1){
					hideDiv("connection");
					displayDiv("info");
		    	}
		    	
		    	mClientId = client.id;
		        channel.publish('connect', 'clientConnect '+client.attributes.name, client.id);
	            title.innerHTML = "Connect "+ client.attributes.name;
	
		    });
	        
		    channel.on('clientDisconnect', function(client){
		    	console.log("clientDisconnect ");
		    	channel.publish('disconnect', 'disconnect '+client.attributes.name, client.id);
	            title.innerHTML = "Disconnected";
	        });
		}
 
 
## Add User Event Listeners

1. Event  'play' : mobile send to 'play' event when select video item.
2. msg : mobile send a json data to TV, json include title,thumbnail url, video url 
 1. we can make youtube video URL using videoId :  http://www.youtube.com/embed/ + "videoId"
 2. and set a url into 'iframe' of id "ytplayer"

	channel.on('play', function(msg, from){
	    	hideDiv("connection");
			hideDiv("info");
			displayDiv("player");
			
	    	var obj = JSON.parse(msg);
	    	
	        console.log("event play : obj "+obj);
	
	        title.innerHTML = "Title : "+ obj.videoName;
	        document.getElementById("thumnail").src = obj.videoThumnail;
	        document.getElementById("ytplayer").src = "http://www.youtube.com/embed/"+obj.videoId+"?autoplay=1&enablejsapi=1";     	        
	        
	        channel.publish('play_TV', 'playing '+ msg, mClientId);
	    });
 
 
