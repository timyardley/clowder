	//TODO: Load from XML file stored in Medici describing institution model.
	var allowedNodes = [["Node 1", "Node"],["Node2", "Node"],["Node3", "Node"],["Node4", "Node"],
						["String1", "Leaf"],["String2", "Leaf"]];
	
	var allowedChildren = [["!root!","Node 1","1","1"],["Node 1","String1", "1", "*"],["Node 1","Node2", "0", "*"],["Node2","String1", "1", "1"],["Node2","Node3", "0", "1"],
						["Node3","String2", "0", "*"]];

	var elementCounter = 1;

	$(function() {
		
		$('body').on('click','.usr_md_,.usr_md_submit',function(e){
			if($(this).is('button')){				
				   if($(this).html() == "Modify"){			  
					var textBox = document.createElement('input');
					textBox.classList.add('usr_md_');		   
					textBox.setAttribute('type', 'text');
					textBox.value = $(this).parent().children("span").get(0).innerHTML;
					   
					$(this).parent().get(0).insertBefore(textBox, $(this).parent().children("span").get(0));					
					$(this).parent().children('span').remove();
					   
					$(this).html("Ok");							   		   			   
				  }
				  else if($(this).html() == "Ok"){
				  	var textSpan = document.createElement('span');
					textSpan.classList.add('usr_md_');
					textSpan.innerHTML = $(this).parent().children("input").get(0).value;
						
					$(this).parent().get(0).insertBefore(textSpan, $(this).parent().children("input").get(0));
					$(this).parent().get(0).removeChild($(this).parent().children("input").get(0));
						 
					$(this).html("Modify");			
				  }			  
				  else if($(this).html() == "Delete"){
				  	$(this).parent().parent().get(0).removeChild($(this).parent().get(0));
				  }
				  else if($(this).html() == "Add property"){
					  
				  	var newProperty = document.createElement("li");
					newProperty.classList.add('usr_md_');
									
					var newPropertyMenu = document.createElement("select");
					newPropertyMenu.classList.add('usr_md_');
									
					var parentNodeType = "";
					if($(this).parent().is('div')){
						parentNodeType = "!root!";
					}
					else{
						parentNodeTypeText = $(this).parent().children('b').get(0).textContent;
						parentNodeType = parentNodeTypeText.substring(0, parentNodeTypeText.length - 1);
					}
					var allowedChildrenForNode = allowedChildren.filter(function (a) {return a[0] == parentNodeType;});
					if(allowedChildrenForNode.length == 0){
						alert("The metadata model states that this property cannot have subproperties of any kind.");
						return false;
					}
					$(this).parent().children('ul')[0].appendChild(newProperty);	
									
					for( var i = 0; i < allowedChildrenForNode.length; i++){
						var newOption = document.createElement("option");
						newOption.classList.add('usr_md_');					
						newOption.setAttribute('value', allowedChildrenForNode[i][1]);
						newOption.innerHTML = allowedChildrenForNode[i][1];
						if(i == 0){
							newOption.setAttribute('selected', 'selected');
						}
						
						newPropertyMenu.appendChild(newOption);					
					}
					newProperty.appendChild(newPropertyMenu);
					
					var newSelectButton = document.createElement('button'); 	
					newSelectButton.classList.add('usr_md_');
					newSelectButton.setAttribute('type','button');		
					
					newSelectButton.innerHTML = 'Select property';
					newProperty.appendChild(newSelectButton);
						
				  }
				  else if($(this).html() == "Select property"){				  	
				  				
					var selectTag = $(this).parent().children('select')[0];
					var selectedProperty = selectTag.options[selectTag.selectedIndex].value;
					var selectedPropertyType = "Node";				
					for(var i = 0; ; i++){
						if(allowedNodes[i][0] == selectedProperty){
							selectedPropertyType = allowedNodes[i][1];
							break;
						}
					} 

					$(this).parent().children('select').remove();

					var newPropertyKey = document.createElement('b');

					newPropertyKey.classList.add('usr_md_');
					newPropertyKey.innerHTML = selectedProperty + ":";
					$(this).parent().get(0).insertBefore(newPropertyKey, $(this).get(0));						
					if(selectedPropertyType == "Leaf"){										
						var textBox = document.createElement('input');
						textBox.classList.add('usr_md_');
									   
						textBox.setAttribute('type', 'text');
						textBox.textContent = "";
						$(this).parent().get(0).insertBefore(textBox, $(this).get(0));
						
						$(this).html("Ok");
						
						var newDeleteButton = document.createElement('button'); 	
						newDeleteButton.classList.add('usr_md_');
						newDeleteButton.setAttribute('type','button');	
						
						newDeleteButton.innerHTML = 'Delete';
						$(this).parent().get(0).appendChild(newDeleteButton);
					}
					else{
						$(this).html("Add property");
						var newDeleteButton = document.createElement('button'); 	
						newDeleteButton.classList.add('usr_md_');
						newDeleteButton.setAttribute('type','button');	
						
						newDeleteButton.innerHTML = 'Delete';
						$(this).parent().get(0).appendChild(newDeleteButton);
						
						var newPropertyList = document.createElement('ul');
						newPropertyList.classList.add('usr_md_');
						 
						$(this).parent().get(0).appendChild(newPropertyList);
					}				
				  }
				  else if($(this).html() == "Submit"){
					var data = DOMtoJSON(document.getElementById('datasetUserMetadata').children[1]);
					
					var request = $.ajax({
				       type: 'POST',
				       url: hostIp,
				       data: JSON.stringify(data),
				       contentType: "application/json",
				     });
					 
					  request.done(function (response, textStatus, jqXHR){
					        console.log("Response " + response);
		     			});
					 
					  request.fail(function (jqXHR, textStatus, errorThrown){
		        		console.error(
		            		"The following error occured: "+
		            		textStatus, errorThrown		            
		        			);
		        		alert("ERROR: " + errorThrown +". Metadata not added." );
		     			});
					 
					 
				  }	
				  
				  return false;
			}
		});
	 
	   
	   $('body').on('keypress','.usr_md_,.usr_md_submit',function(e){
	   	if($(this).is('input')){
		   	if(e.which == 13){
				$($(this).parent().children('button')[0]).click();			
				return false;
			}
		}
	   });
	   
	   function DOMtoJSON(branchRootNode){
	   			var branchData = {};				
				var childrenProperties = branchRootNode.children;
				for(var i = 0; i < childrenProperties.length; i++){
					var key = childrenProperties[i].children[0].innerHTML;
					key = key.substring(0, key.length - 1) + "__" + elementCounter;
					elementCounter++;
					if(childrenProperties[i].children[1].tagName.toLowerCase() == 'span'){
						branchData[key] = childrenProperties[i].children[1].innerHTML;
					}else{
						branchData[key] = DOMtoJSON(childrenProperties[i].children[3]);
					}
				}
				return branchData;
	   }
	   	   	  
	 });