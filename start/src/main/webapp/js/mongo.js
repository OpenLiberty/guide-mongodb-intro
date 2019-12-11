/*******************************************************************************
* Copyright (c) 2019 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/

function addCrewMember() {
	var crewMember = {};
	crewMember.name = document.getElementById("crewMemberName").value;
	var rank = document.getElementById("crewMemberRank");
	crewMember.rank = rank.options[rank.selectedIndex].text;
	crewMember.crewID = document.getElementById("crewMemberID").value;


	var request = new XMLHttpRequest();

	request.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
			if (this.response != '') {
                var res = JSON.parse(this.response);
                if (Array.isArray(res) == true) {
                    for (m of JSON.parse(this.response)) {
                        toast(m, i++);
                    }
                 }
			}
		}
		refreshDocDisplay();
	}

	request.open("POST", "crew/"+crewMember.crewID, true);
	request.setRequestHeader("Content-type", "application/json");
	request.send(JSON.stringify(crewMember));
}

function showUpdateForm(id, name, crewID, rank) {
    if (document.getElementById("docID").value === id) {
        clearUpdateForm();
        return;
    }

    document.getElementById("userUpdate").classList.remove("hidden");

    document.getElementById("docID").value = id;
    document.getElementById("updateCrewMemberName").value = name;
    document.getElementById("updateCrewMemberID").value = crewID;
    document.getElementById("updateCrewMemberRank").value = rank;
}

function clearUpdateForm() {
    document.getElementById("userUpdate").classList.add("hidden");

    document.getElementById("docID").value = "";
    document.getElementById("updateCrewMemberName").value = "";
    document.getElementById("updateCrewMemberID").value = "";
    document.getElementById("updateCrewMemberRank").value = "Captain";
}

function updateCrewMember() {
    var id = document.getElementById("docID").value;

    var crewMember = {};
    crewMember.name = document.getElementById("updateCrewMemberName").value;
    var rank = document.getElementById("updateCrewMemberRank");
    crewMember.rank = rank.options[rank.selectedIndex].text;
    crewMember.crewID = document.getElementById("updateCrewMemberID").value;

    var request = new XMLHttpRequest();

    request.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            if (this.response != '') {
                var res = JSON.parse(this.response);
                if (Array.isArray(res) == true) {
                    for (m of JSON.parse(this.response)) {
                        toast(m, i++);
                    }
                }
            }
        }
        refreshDocDisplay();
    }

    request.open("PUT", "crew/"+id, true);
    request.setRequestHeader("Content-type", "application/json");
    request.send(JSON.stringify(crewMember));

    clearUpdateForm();
}

function refreshDocDisplay() {
	var request = new XMLHttpRequest();
	
	request.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
		    if (this.response != '') {
		        clearDisplay()
                doc = JSON.parse(this.responseText);

                doc.forEach(addToDisplay);
                if (doc.length > 0) {
                    document.getElementById("userDisplay").style.display = 'flex';
                    document.getElementById("docDisplay").style.display = 'flex';
                } else {
                    document.getElementById("userDisplay").style.display = 'none';
                    document.getElementById("docDisplay").style.display = 'none';
                    clearUpdateForm();
                }
                document.getElementById("docText").innerHTML = JSON.stringify(doc,null,2);
		    }
		}
	}

	request.open("GET", "crew/", true);
	request.send();
}

function addToDisplay(entry){
	var userHtml =	"<div>Name: " + entry.Name + "</div>" +
					"<div>ID: " + entry.CrewID + "</div>" +
					"<div>Rank: " + entry.Rank + "</div>" +
					"<button class=\"deleteButton\" onclick=\"remove(event,'"+entry._id.$oid+"')\">Delete</button>";
					
	var userDiv = document.createElement("div");
	userDiv.setAttribute("class","user flexbox");
	userDiv.setAttribute("id",entry._id.$oid);
	userDiv.setAttribute("onclick","showUpdateForm('"+entry._id.$oid+"','"+entry.Name+"','"+entry.CrewID+"','"+entry.Rank+"')");
	userDiv.innerHTML=userHtml;
	document.getElementById("userBoxes").appendChild(userDiv);
}

function clearDisplay(){
	var usersDiv = document.getElementById("userBoxes");
	while (usersDiv.firstChild) {
		usersDiv.removeChild(usersDiv.firstChild);
	}
}

function remove(e, id) {
	var request = new XMLHttpRequest();
	
	request.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
		    if (this.response != '') {
		        if (id === document.getElementById("docID").value) {
                    clearUpdateForm();
                }
                document.getElementById(id).remove();
                refreshDocDisplay()
		    }
		}
	}

	request.open("DELETE", "crew/"+id, true);
	request.send();

	e.stopPropagation();
}

function toast(message, index) {
	var length = 3000;
	var toast = document.getElementById("toast");
	setTimeout(function(){ toast.innerText = message; toast.className = "show"; }, length*index);
	setTimeout(function(){ toast.className = toast.className.replace("show",""); }, length + length*index);
}