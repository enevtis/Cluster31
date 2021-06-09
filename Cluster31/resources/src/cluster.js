

function getTitleForStatus(status) {

	switch (status) {
		case "U":
			return "running";
			break;
		case "Q":
			return "unknown";
			break;
		case "W":
			return "waiting";
			break;
		case "E":
			return "error";
			break;
		case "D":
			return "stopped";
			break;

		default:
			return "unknown";
			break;

	}

}




function getStatus(url, callback) {
	var xhr = new XMLHttpRequest;

	xhr.open("GET", url);
	xhr.send();
	xhr.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {

			var response = xhr.responseText;
			//            console.log('response='+response);
			if (callback) callback(response);
		};
	}
}
function addLabel(id, text) {
	var color = '#FFF';
	var objRect = document.getElementById(id).getBoundingClientRect();
	var label1 = document.createElement('div');
	var curStyle = 'position:absolute;';

	curStyle += 'left:' + objRect.left + 'px;';
	curStyle += 'top:' + (objRect.top - 20) + 'px;';
	curStyle += 'font-size: small;';

	label1.style.cssText = curStyle;
	label1.innerHTML = text;
	document.body.appendChild(label1);
}

function showMenu(divId) {

	var rect = document.getElementById(divId).getBoundingClientRect();

	menuBox = window.document.querySelector('.menu');
	menuBox.style.left = (rect.left + 16) + 'px';
	menuBox.style.top = (rect.top + 16) + 'px';
	menuBox.style.display = 'block';
}

window.addEventListener('click', function(event) {
	menuBox = window.document.querySelector('.menu');
	if( menuBox ) {
		menuBox.style.display = 'none';
	}


}, true); 