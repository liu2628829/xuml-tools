<html>
<head>
<script type="text/javascript"> 
function callUrl(url, method) {
  var client = new XMLHttpRequest();
  client.open(method, url, true);
  client.setRequestHeader("Content-Type", "text/plain");
  client.onreadystatechange = function() {
      var msg;
      if (client.status == 200)
        msg = client.responseText;
      else
        msg = "Request failed. status=" + client.status + " " + client.statusText;
      //document.getElementById("message").innerHTML=msg;
  }
  client.send();
} 

function signal(name) {
	callUrl("rest/order/123/" + name,'PUT');
}
  
(function() {
  var eventSource = new EventSource("sse");
  eventSource.onmessage = function(event) {
      document.getElementById("message").innerHTML=event.data;
  };
  
})();
</script>
</head>
<body>

<h2>Order tracker</h2>
<ul>
	<li><a href="#" onclick="callUrl('rest/order/123/create?description=an order&fromAddress=12 Something St, Canberra&toAddress=144 Bank St, Dickson&comment=created&destinationEmail=recipient@goog.com&senderEmail=online.company@goog.com&maxAttempts=3','POST');">Create an order</a></li>
	<li><a href="#" onclick="signal('send');">Send the order</a></li>
	<li><a href="#" onclick="signal('assign');">Assign to a courier</a></li>
	<li><a href="#" onclick="signal('pickedUp');">Order picked up by courier</a></li>
	<li><a href="#" onclick="signal('arrivedDepot?depotId=1');">Arrived depot</a></li>
	<li><a href="#" onclick="signal('arrivedFinalDepot?depotId=1');">Arrived final depot</a></li>
	<li><a href="#" onclick="signal('delivering');">Delivering</a></li>
	<li><a href="#" onclick="signal('delivered');">Delivered</a></li>
	<li><a href="#" onclick="signal('deliveryFailed');">Delivery failed</a></li>
	<li><a href="#" onclick="signal('deliveryAgain');">Deliver again</a></li>
	<li><a href="rest/depot/1/ordersReadyForDelivery">Orders for delivery at depot 1</a></li>
	<li><a href="rest/order/123/status">Status of order 123</a></li>
</ul>

<p><div id="message"/></p>

</body>
</html>