
$(function(){
	//src="http://www.youtube.com/embed/9ZfN87gSjvI">

    var title = document.getElementById("title");
    
	var mChannel = 'com.samsung.msf.youtubetest';
	
	var mClientId = null;
	var fullscreen = false;
//	hideDiv("connection");
//	hideDiv("info");
//	displayDiv("player");
	
	msf.local(function(err, service){
		var reqAppControl = tizen.application.getCurrentApplication().getRequestedAppControl();

		if (reqAppControl && reqAppControl.appControl) {
		    var data = reqAppControl.appControl.data;
		    
		    for (var i = 0; i < data.length; i++) {
		    	log("WRT param #" + i + " key : " + data[i].key);
		        for (var j = 0; j < data[i].value.length; j++) {
		            console.log("WRT param #" + i + " #" + j + " value : " + data[i].value[j]);
		        }
		        if (data[i].key == "PAYLOAD") {
		        	log("payload =" + data[i].value[0]);
		        	var payload = data[i].value[0];
		        }
		    }
		}
		
		log('Start MSF');
	    var channel = service.channel(mChannel);
	    
	    log('Start MSF Channel = '+ mChannel);
	    
	    channel.connect({name: 'TV'}, function (err) {
	        if(err) {
	        	return console.log(err);
	        }
	        log('channel.connect');
	    });
	
	    channel.on('connect', function(client){
	    	log("connect client.id :"+client.id);
	    	console.log("connect channel.isConnected :"+channel.isConnected);
	    	console.log("connect channel.isConnected :"+channel.clients.length);
	    	// if connect TV with client(mobile)

	    	if(channel.clients.length > 1){
				hideDiv("connection");
				displayDiv("info");
	    	}
	    });
	    
	    channel.on('disconnect', function(client){
	    	log("disconnect ");
	    });
	    
	    channel.on('clientConnect', function(client){
	    	log("clientConnect ");

	    	if(channel.clients.length > 1){
				hideDiv("connection");
				displayDiv("info");
	    	}
	    	
	    	mClientId = client.id;
	        channel.publish('connect', 'clientConnect '+client.attributes.name, client.id);
            title.innerHTML = "Connect "+ client.attributes.name;

	    });
        
	    channel.on('clientDisconnect', function(client){
	    	log("clientDisconnect ");
	    	channel.publish('disconnect', 'disconnect '+client.attributes.name, client.id);
            title.innerHTML = "Disconnected";
        });
	    
	    channel.on('play', function(msg, from){
	    	hideDiv("connection");
			hideDiv("info");
			displayDiv("player");
			
						
	    	var obj = JSON.parse(msg);
	    	
	        log("event play : obj "+obj);
 
            title.innerHTML = "Title : "+ obj.videoName;
            document.getElementById("thumnail").src = obj.videoThumnail;
            
            readyPlayer(obj.videoId);
            
            //document.getElementById("ytplayer").src = "http://www.youtube.com/embed/"+obj.videoId+"?autoplay=1&enablejsapi=1";     	        
            
            channel.publish('play_TV', 'playing '+ msg, mClientId);
	    });
	
	    channel.on('control', function(msg, from){
	    	log("event control : msg "+msg);
	    	if(msg == 'play')
	    		play();
	    	else if(msg == 'pause')
	    		puase();
	    	else if(msg == 'stop')
	    		stop();
	    	else if(msg == 'ff')
	    		ff();
	    	else if(msg == 'rew')
	    		rew();
	    	else if(msg == 'fullScreen'){
	    		 fullscreen = true;
	    		 $('#ytplayer').toggleClass('fullscreen'); 
	    		 $('#youtubePlayer').attr("width","100%");
	    		 $('#youtubePlayer').attr("height","100%");
	    	}
	    		
	    	else if(msg == 'originScreen'){
	    		 fullscreen = false;
	    		 $('#ytplayer').toggleClass('fullscreen');
	    		 $('#youtubePlayer').attr("width","640");
	    		 $('#youtubePlayer').attr("height","480");
	    	}
	    	
	    	channel.publish('contorl_TV', msg, mClientId);
	    });
	
	});

	function displayDiv(idMyDiv){
	     document.getElementById(idMyDiv).style.display="block";
	}

	function hideDiv(idMyDiv){
	    document.getElementById(idMyDiv).style.display = "none";
	}
	
	function log(msg) {
        jQuery('#log').prepend(msg + '<br/>');
	}
	
	function onPlayerStateChange(event) {
	    switch(event.data) {
	        case YT.PlayerState.ENDED:
	            log('Video has ended.');
	            break;
	        case YT.PlayerState.PLAYING:
	            log('Video is playing.');
	            break;
	        case YT.PlayerState.PAUSED:
	            log('Video is paused.');
	            break;
	        case YT.PlayerState.BUFFERING:
	            log('Video is buffering.');
	            break;
	        case YT.PlayerState.CUED:
	            log('Video is cued.');
	            break;
	    }
	}
	
	function play(){
		player.playVideo();
	}
	
	function stop(){
		player.stopVideo();
	}
	
	function puase(){
		player.pauseVideo();
	}
	
	function ff(){
        var ct = player.getCurrentTime();
		player.seekTo(ct+10,true);
	}
	
	function rew(){
        var ct = player.getCurrentTime();
		player.seekTo(ct-10,true);
	}
	
	function readyPlayer(vidId){
		if(fullscreen){
			fullscreen = false;
			$('#ytplayer').toggleClass('fullscreen'); 
		}
		
        $('#ytplayer').html('<iframe id="youtubePlayer" width="640" height="480" src="http://www.youtube.com/embed/' + vidId + '?enablejsapi=1&autoplay=1&autohide=1&showinfo=0&fs=0" frameborder="0" allowfullscreen></iframe>');	
	
        player = new YT.Player('youtubePlayer', {
            events: {
                'onStateChange': onPlayerStateChange
            }
        });
	}

	
//	jQuery(document).ready(function($) {
	        
//        $('#ytplayer').html('<iframe id="player_'+vidId+'" width="420" height="315" src="http://www.youtube.com/embed/' + vidId + '?enablejsapi=1&autoplay=1&autohide=1&showinfo=0" frameborder="0" allowfullscreen></iframe>');	
//        
//        player = new YT.Player('player_'+vidId, {
//            events: {
//                'onStateChange': onPlayerStateChange
//            }
//        });
//	});
	
});


