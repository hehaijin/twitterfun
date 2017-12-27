
 var url="ws://localhost:9000/tweets";
    var tweetSocket=new WebSocket(url);
    tweetSocket.onmessage= function(event){
        console.log(event.data);
		var data=event.data;
        //var data= JSON.parse(event.data);
        var tweet=document.createElement("p");
        var text=document.createTextNode(data);
        tweet.appendChild(text);
        document.getElementById("tweets").appendChild(tweet);
	}
