var defaultMeasureFrameAdapter = new function () {
	var getPosition = function(obj) {
    var x = 0,
        y = 0;
    while (obj) {
        x += obj.offsetLeft || 0;
        y += obj.offsetTop || 0;
        obj = obj.offsetParent || obj.parentNode;
    }
		return {
			x: x,
			y: y
		};
	};
	
	var getScroll = function(obj) {
    var x = 0,
        y = 0;
    while (obj) {
        x += obj.scrollLeft || 0;
        y += obj.scrollTop || 0;
        obj = obj.offsetParent || obj.parentNode;
    }
		return {
			x: x,
			y: y
		};
	};
	
	var copyItems = function(arrayTo, arrayFrom) {
		if (arrayFrom) {
			for (var item in arrayFrom) {
				arrayTo[item] = arrayFrom[item];
			}
		}
		return arrayTo;
	};

    var _getBorderRect = function (node) {
        var position = getPosition(node);
        var width = node.offsetWidth;
        var height = node.offsetHeight;

        var scroll = getScroll(node);
        position.x -= scroll.x;
        position.y -= scroll.y;

        return {
            position: {
                x: position.x,
                y: position.y,
            },
            width: width,
            height: height
        };
    };
    this.getBorderRect = function (node) {
        return _getBorderRect(node);
    };

    var _deleteFrame = function (item) {
        document.body.removeChild(item.border);
    };
    this.deleteFrame = function (item) {
        _deleteFrame(item);
    };

    this.setFrame = function (item) {
        _setFrame(item);
    };
};


var android = new function(){
	var selectionStartRange = null;
	var selectionEndRange = null;
	var lastTouchPoint = null;
	var cTarget=null;
	var self=this;
	var t=null;
	this.canClip = false;
	//var borderRect=null;
	this.article=null;
	var _border = {
        node: undefined,
		rect: undefined
    }
	
	var _stack = [];
	this.startTouch = function(x, y){
		//this.longTouch();
	};
	
	this.fvdSaveCss = function(b){
		fvdSaveCss = b;
	}
	
	this.setClip = function(b){
		this.canClip = b;
	}
	
	this.ZoomInSelection = function(){
		if (_stack.length>0 && _stack[0].action==1){
			window.TextSelection.selectionChanged("action:Page1", "", null, null);
			var b = _stack.shift();
			_border.node = b.node;
			cTarget = _border.node;
			selectionChanged();
		}
		else {
			var parent = _border.node.parentNode;
			
			if (parent){
				_stack.unshift({action:0, node: _border.node});
				cTarget = parent;
				if (true) {
						_border.node = cTarget;
						selectionChanged();
				}
			}
		}
		
	}
	
	this.ZoomOutSelection = function(){
		window.TextSelection.selectionChanged("action:Page0", "", null, null);
		if (_stack.length>0 && _stack[0].action==0){
			var b = _stack.shift();
			_border.node = b.node;
			cTarget = _border.node;
			selectionChanged();
		}
		else {
			if(_border.node.childNodes.length>0){
				var parent = null;
				for(var i = 0; i<_border.node.childNodes.length; i++){
					var nodeName=(_border.node.childNodes[i].tagName?_border.node.childNodes[i].tagName.toUpperCase():"");
					if(typeof _border.node.childNodes[i] == 'string' || nodeName=="DIV" || nodeName=="SPAN"  || nodeName=="UL" || nodeName=="LI" || nodeName=="P"){
						parent=_border.node.childNodes[i];
						break;
					}
				}
				
				if (parent){
					_stack.push({action:1, node: _border.node});
					cTarget = parent;
					if (true) {
						_border.node = cTarget;
						selectionChanged();
					}
				} else window.TextSelection.selectionChanged("action:reset", "reset", null, null);
			}
		}
		
	}
	
	this.savePage=function(){
        var html= _getArticle(document.body,false);
		window.TextSelection.selectionChanged("action:savePage", html, null, null);
	}
	
	this.saveArticle=function(){
        var obj= this.article.getContent();
		var html = _getArticle(obj.html,true);
		//var html = obj.html.innerHTML;
		window.TextSelection.selectionChanged("action:saveArticle", html , null, null);
	}
	
	
	this.selectVisible = function(){
		alert(document.body.clientWidth);
		var x = document.body.clientWidth/3;
		var y = Math.floor(document.body.scrollTop + fvdViewHeight/2);
		cTarget = document.elementFromPoint(x,y);
		var i=0;
		while(!cTarget&&i<50){
			y+=5;
			cTarget = document.elementFromPoint(x,y);
			i++;
		}
		
		if(cTarget){
			lastTouchPoint = {'x': x, 'y': y};
			var sel = window.getSelection();
			var range = document.createRange();
			range.selectNode(cTarget);
			sel.addRange(range);
			this.saveSelectionStart();
			this.saveSelectionEnd();
			selectionChanged();
		}
		else window.TextSelection.jsError("selectVisible - cTarget is null");
	}
	
	this.hasSelection = function(){
		return (_border.node!=null);
	};
	
	var excludedStyle = ["unicode-bidi"];
	
	var _isIncludedStyle = function (styleName) {
        return excludedStyle.indexOf(styleName) == -1;
    };
	
	this.clearSelection = function(){
		try{
			var sel = window.getSelection();
			if (sel){
				sel.removeAllRanges();
			}
			
			_border.node=null;
		}
		catch(err){
			window.TextSelection.jsError("clearSelection - " +err);
		}	
	};
	
	this.longTouch = function() {
		try{
			//this.clearSelection();
			_stack = [];
			if(cTarget){
				if (true) {
					_border.node = cTarget;
					selectionChanged();
				}
			}
			else {
				window.TextSelection.selectionChanged(null, null, null, null);
			}
		}
		catch(err){
			window.TextSelection.jsError("longTouch - " + err);
		}
	};
	
	var getPos = function(ele){
		var x = 0, y = 0;
		while (ele) {
			x += ele.offsetLeft || 0;
			y += ele.offsetTop || 0;
			ele = ele.offsetParent || ele.parentNode;
		}
		
		return [x, y];
	}
	
	var _getSVG = function (node) {
        var cloneSVG = node.cloneNode(true);
        var divTmp = document.createElement("div");
        divTmp.appendChild(cloneSVG);
        return divTmp.innerHTML;
    };

	
	var _isNodeVisible = function(node) {
        if (!node) {
            return false;
        }
        if (node.type && node.type == "hidden") {
            return false;
        }
        
        try{
	        var compStyles = document.defaultView.getComputedStyle(node, null);
	    }
		catch(ex){}
			if(compStyles)
				return compStyles.getPropertyValue("display") != "none";
			else 
				return false;
			
			 
    };
	
	var _isExclude = function(attrName) {
        if (excludeAttrs.indexOf(attrName) != -1) {
            return true;
        } else {
            return false;
        }
    };

	
	var _getAttributes = function(node) {
        if (node.hasAttributes()) {
            var attributesString = '';
            var attrs = node.attributes;
            for (var i = 0; i < attrs.length; i++) {
                if (!_isExclude(attrs[i].name)) {
                    attributesString += ' ' + attrs[i].name + "='";
                    try {
                        attributesString += _processAttrValue(attrs[i], node) + "' ";
                    } catch (e) {
                        //console.warn(e)
                        attributesString += "' ";
                    }
                }
            }
            return attributesString;
        }
        return '';
    };
	
	var _processAttrValue = function(attr, element) {
        if (attr.name == 'src') {
            return element.src;
        } else if (attr.name == 'href') {
            return element.href;
        } else {
            return /*htmlEncode*/(attr.value);
        }
    };

	var _NODE_NAME_TRANSLATIONS = {
        "HTML": "DIV",
        "BODY": "DIV",
        "FORM": "DIV",
        "CANVAS": "DIV",
        "CUFON": "DIV",
        "BDI": "SPAN",
        "*": "DIV",
        "WBR" : "SPAN"
    };
	
	var _replaceNodeName = function(nodeName) {
        var nodeName = _NODE_NAME_TRANSLATIONS[nodeName.toUpperCase()] || nodeName.toUpperCase();
        return (typeof _SUPPORTED_NODES[nodeName] != "undefined") ? nodeName : _NODE_NAME_TRANSLATIONS["*"];
    };
	
	var _articleStyles = [
		{
            tags: 'img',
            classes: '*',
            style: 'max-width:100%; height:auto; margin:0 0 20px 0;'
        },
        {
            tags: 'a',
            classes: '*',
            style: 'cursor:pointer; text-decoration:none; outline:none; color:#19a1b9;'
        },
        {
            tags: '*',
            classes: 'center',
            style: 'margin: 20px auto 30px;min-width: 300px;padding: 0 10px;width: 60%;'
        },
        {
            tags: '*',
            classes: '',
            style: 'margin:0; padding:0;'
        },
        {
            tags: 'p',
            classes: '*',
            style: 'padding:0 0 20px 0;'
        },
        {
            tags: 'ul',
            classes: '*',
            style: 'padding:0 0 30px 0;'
        },
		{
            tags: 'ol',
            classes: '*',
            style: 'padding:0 0 30px 20px;'
        },
		{
            tags: 'li',
            classes: 'ul',
            style: 'padding:0 0 0 20px; position:relative;'
        },
        {
            tags: 'h1',
            classes: '*',
            style: 'font-weight:bold; font-size:32px; color:#2c2c2c; padding:0 0 30px 0;'
        },
		{
            tags: 'h2',
            classes: '*',
            style: 'font-size:24px; font-weight:normal; padding:0 0 20px 0;'
        },
		{
            tags: 'h3',
            classes: '*',
            style: 'font-size:21px; font-weight:normal; padding:0 0 20px 0;'
        },
		{
            tags: 'h4',
            classes: '*',
            style: 'font-size:18px; font-weight:normal; padding:0 0 20px 0;'
        },
		{
            tags: 'h5',
            classes: '*',
            style: 'font-size:16px; font-weight:bold; padding:0 0 20px 0;'
        },
		{
            tags: 'h6',
            classes: '*',
            style: 'font-size:15px; font-weight:bold; padding:0 0 20px 0;'
        }
    ];

	
	
    var _getStyleForArticleElement = function(e, styles) {
        var styles = _articleStyles;
        var l = styles.length;
        //var style = ' style="font-family:Arial,Helvetica,sans-serif;';
		var style = ' style="font-family:Georgia, Arial,Helvetica;';
        for (var i = 0; i < l; ++i) {
            s = styles[i];
            if (s.tags.indexOf( e.tagName.toLocaleLowerCase() ) > -1) {
                style += s.style;
            }
            try {
                if (s.classes === '*') {
                    continue;
                }
                for (var j = e.classList.length - 1; j >= 0; j--) {
                    if (s.classes.indexOf( e.classList.item(j) ) > -1) {
                        style += s.style;
                    }
                }
            } catch (e) {continue;}
        }
        return style + '" ';
    }

	
	var _SUPPORTED_NODES = {
        "A": null,
        "ABBR": null,
        "ACRONYM": null,
        "ADDRESS": null,
        "AREA": null,
        "B": null,
        "BASE": null,
        "BASEFONT": null,
        "BDO": null,
        "BIG": null,
        "BLOCKQUOTE": null,
        "BR": null,
        "BUTTON": null,
        "CAPTION": null,
        "CENTER": null,
        "CITE": null,
        "CODE": null,
        "COL": null,
        "COLGROUP": null,
        "DD": null,
        "DEL": null,
        "DFN": null,
        "DIR": null,
        "DIV": null,
        "DL": null,
        "DT": null,
        "EM": null,
        "FIELDSET": null,
        "FONT": null,
        "FORM": null,
        "FRAME": null,
        "FRAMESET": null,
        "H1": null,
        "H2": null,
        "H3": null,
        "H4": null,
        "H5": null,
        "H6": null,
        "HR": null,
        "HTML": null,
        "I": null,
        "IFRAME": null,
        "IMG": null,
        "INPUT": null,
        "INS": null,
        "KBD": null,
        "LABEL": null,
        "LEGEND": null,
        "LI": null,
        "LINK": null,
        "MAP": null,
        "MENU": null,
        "META": null,
        "NOBR": null,
        "NOFRAMES": null,
        "OBJECT": null,
        "PARAM": null,
        "EMBED": null,
        "OL": null,
        "P": null,
        "PRE": null,
        "Q": null,
        "QUOTE": null,
        "S": null,
        "SAMP": null,
        "SMALL": null,
        "SPAN": null,
        "STRIKE": null,
        "STRONG": null,
        "SUB": null,
        "SUP": null,
        "TABLE": null,
        "TBODY": null,
        "TD": null,
        "TEXTAREA": null,
        "TFOOT": null,
        "TH": null,
        "THEAD": null,
        "TITLE": null,
        "TR": null,
        "TT": null,
        "U": null,
        "UL": null,
        "VAR": null,
        "VIDEO": null,
        "SELECT": null,
        "OPTION": null,
        "NOINDEX":null
    };

    var htmlEncode = function(str) {
        return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&apos;');
    };
    var htmlDecode = function(str) {
        return str.replace(/&amp;/g, '&').replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&quot;/g, '"').replace(/&apos;/g, '\'');
    };


	var excludeAttrs = ['class', 'style', 'onblur', 'onchange', 'onclick', 'ondblclick', 'onfocus', 'onkeydown', 'onkeypress', 'onkeyup', 'onload', 'onmousedown', 'onmousemove', 'onmouseout', 'onmouseover', 'onmouseup', 'onreset', 'onselect', 'onsubmit', 'onunload'];
    var excludedElementClass = ['nimbus_fragment', 'nimbusNode', 'notifyIcon', 'copyIcon', 'pasteIcon', 'nimbus_alert', 'nimbus_images', 'nimbus_measure', 'nimbus_line', 'selectedElementBorder'];


	var _requiredStyles=[
		//{property:"cssText", nullValue: "background-attachment"}
	//{property:"alignment-baseline", nullValue: "#"},
	//{property:"background-clip", nullValue: "border-box"},
	{property:"background-color", nullValue: "rgb(255, 255, 255)"},
	{property:"background-image", nullValue: "none"},
	//{property:"background-origin", nullValue: "padding-box"},
	{property:"background-position-x", nullValue: "0%"},
	{property:"background-position-y", nullValue: "0%"},
	{property:"background-repeat", nullValue: "#"},
	//{property:"background-size", nullValue: "auto auto"},
	//{property:"baseline-shift", nullValue: "baseline"},
	{property:"border-bottom-color", nullValue: "rgb(0, 0, 0)"},
	{property:"border-bottom-left-radius", nullValue: "0px"},
	{property:"border-bottom-right-radius", nullValue: "0px"},
	{property:"border-bottom-style", nullValue: "none"},
	{property:"border-bottom-width", nullValue: "0px"},
	{property:"border-collapse", nullValue: "separate"},
	{property:"border-left-color", nullValue: "#"},
	{property:"border-left-style", nullValue: "none"},
	{property:"border-left-width", nullValue: "0px"},
	{property:"border-right-color", nullValue: "rgb(0, 0, 0)"},
	{property:"border-right-style", nullValue: "none"},
	{property:"border-right-width", nullValue: "0px"},
	{property:"border-top-color", nullValue: "#"},
	{property:"border-top-left-radius", nullValue: "0px"},
	{property:"border-top-right-radius", nullValue: "0px"},
	{property:"border-top-style", nullValue: "none"},
	{property:"border-top-width", nullValue: "0px"},
	{property:"bottom", nullValue: "auto"},
	{property:"box-shadow", nullValue: "none"},
	{property:"box-sizing", nullValue: "content-box"},
	{property:"clear", nullValue: "none"},
	{property:"color", nullValue: "#"},
	//{property:"cursor", nullValue: "#"},
	{property:"display", nullValue: "block"},
	/*{property:"float", nullValue: "none"},*/
	{property:"font-family", nullValue: "#"},
	{property:"font-size", nullValue: "#"},
	{property:"font-style", nullValue: "normal"},
	{property:"font-weight", nullValue: "normal"},
	{property:"height", nullValue: "0px"},
	{property:"left", nullValue: "auto"},
	{property:"list-style-image", nullValue: "none"},
	{property:"list-style-position", nullValue: "outside"},
	{property:"list-style-type", nullValue: "disc"},
	{property:"margin-bottom", nullValue: "#"},
	{property:"margin-left", nullValue: "#"},
	{property:"margin-right", nullValue: "#"},
	{property:"margin-top", nullValue: "#"},
	{property:"overflow-x", nullValue: "visible"},
	{property:"overflow-y", nullValue: "visible"},
	{property:"padding-bottom", nullValue: "0px"},
	{property:"padding-left", nullValue: "0px"},
	{property:"padding-right", nullValue: "0px"},
	{property:"padding-top", nullValue: "0px"},
	{property:"position", nullValue: "static"},
	{property:"right", nullValue: "auto"},
	{property:"table-layout", nullValue: "auto"},
	{property:"text-align", nullValue: "start"},
	{property:"text-decoration", nullValue: "none"},
	{property:"text-indent", nullValue: "0px"},
	{property:"text-overflow", nullValue: "clip"},
	{property:"text-shadow", nullValue: "none"},
	{property:"top", nullValue: "auto"},
	//{property:"visibility", nullValue: "visible"},
	{property:"width", nullValue: "0px"},
	];
	

		//added
	var trim = function (s){
		return s.replace(/^\s+|\s+$/g, "");
	}
	
	var styleCollection = function () {
        var result = [];

        result.hasStyle = function (item) {
			//return false;
            for (var i = 0; i < this.length; i++) {
                if (this[i].name == trim(item.name) && this[i].value == item.value && this[i].level<=item.level) {
                    return true;
                }
            }
            return false;
        };
        result.getStyle = function (name) {
            for (var i = 0; i < this.length; i++) {
                if (this[i].name == trim(name)) {
                    return this[i];
                }
            }
            return;
        };
		
        result.addStyle = function (style) {
            var prop = trim(style.name);
            var lvl = style.level;
            var value = style.value;
			this.push({
                        name: prop,
                        value: value,
						level: lvl
                        //priority: priority
					});
            /*if ((!this.hasStyle(prop)) {
                if (prop && prop.length > 0 && value) {
                    for (var i = 0; i < this.length; i++) {
                        if (this[i].name == prop) {
                            this.splice(i, 1);
                            break;
                        }
                    }

                    this.push({
                        name: prop,
                        value: value,
                        //priority: priority
                    });
                }
            }*/
        };

        result.removeStyle = function (name) {
            var index = this.indexOf(name);
            if (index != -1) {
                this.splice(index, 1);
            }
        };

        result.excludeStyle = function (styles) {
            if (styles && styles.length > 0) {
                for (var i = 0; i < styles.length; i++) {
                    var index = this.indexOf(styles[i]);
                    if (index != -1) {
                        this.splice(index, 1);
                    }
                }
            }
        };

        result.toString = function () {
            var str = "";
            for (var i = 0; i < this.length; ++i) {
                var styleName = this[i].name;
                var value = this[i].value;
                if (value != null && value.length > 0) {
                    str += styleName + ":" + value + ";";
                }
            }
            return str;
        };

        return result;
    };
	//

	var _INHERITED_STYLES = [
        "azimuth",
        "border-collapse", "border-spacing",
        "caption-side", /*"color",*/ "cursor",
        "elevation", "empty-cells",
        "font-family"/*, "font-size", "font-style", "font-weight", "font"*/,
        "pitch-range", "pitch",
        "text-align", "text-indent", "text-transform",
        "visibility"//, "voice-family", "volume",
    ];
	
	var _TEXT_NODES =[
		'p', 'a',
		'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'tr', 'td', 'tbody', 'table','th',
	];
	
	var _TABLE_NODES =[
		'tr', 'td', 'tbody', 'table','th',
	];
	
	var _textStyles=[
		{property:"background-color", nullValue: "rgb(0, 0, 0)"},
		{property:"background-image", nullValue: "none"},
		{property:"background-position-x", nullValue: "0%"},
		{property:"background-position-y", nullValue: "0%"},
		{property:"background-repeat", nullValue: "#"},
		{property:"color", nullValue: "#"},
		{property:"float", nullValue: "none"},
		{property:"font-family", nullValue: "#"},
		{property:"font-size", nullValue: "#"},
		{property:"font-style", nullValue: "normal"},
		{property:"font-weight", nullValue: "normal"},
		{property:"margin-bottom", nullValue: "#"},
		{property:"margin-left", nullValue: "#"},
		{property:"margin-right", nullValue: "#"},
		{property:"margin-top", nullValue: "#"},
		{property:"padding-bottom", nullValue: "0px"},
		{property:"padding-left", nullValue: "0px"},
		{property:"padding-right", nullValue: "0px"},
		{property:"padding-top", nullValue: "0px"},
		{property:"text-align", nullValue: "start"},
		{property:"text-decoration", nullValue: "none"},
	];
	
	var _tableStyles=[
		{property:"background-color", nullValue: "rgb(0, 0, 0)"},
		{property:"background-image", nullValue: "none"},
		{property:"color", nullValue: "#"},
		{property:"font-weight", nullValue: "normal"},
	];
	
	var _imgStyles=[
		{property:"border-bottom-color", nullValue: "rgb(0, 0, 0)"},
		{property:"border-bottom-style", nullValue: "none"},
		{property:"border-bottom-width", nullValue: "0px"},
		{property:"border-collapse", nullValue: "separate"},
		{property:"border-left-color", nullValue: "#"},
		{property:"border-left-style", nullValue: "none"},
		{property:"border-left-width", nullValue: "0px"},
		{property:"border-right-color", nullValue: "rgb(0, 0, 0)"},
		{property:"border-right-style", nullValue: "none"},
		{property:"border-right-width", nullValue: "0px"},
		{property:"border-top-color", nullValue: "#"},
		{property:"border-top-style", nullValue: "none"},
		{property:"border-top-width", nullValue: "0px"},
		{property:"display", nullValue: "block"},
		{property:"float", nullValue: "none"},
		{property:"margin-bottom", nullValue: "#"},
		{property:"margin-left", nullValue: "#"},
		{property:"margin-right", nullValue: "#"},
		{property:"margin-top", nullValue: "#"},
		{property:"width", nullValue: "auto"},
		{property:"height", nullValue: "auto"},
	];
	
	var _TEXT_PROP = [
		"color", "font-family", "font-size", "font-style", "font-weight", "text-align",
	];
	
	var _TABLE_PROP = [
		"color", "font-size", //"font-family", "font-style", "font-weight", "text-align",
	];
	
	var _canPatch = function(tag, prop){
		if(prop=="height" || prop =="width"){
			if(tag)	
			{
				return (tag.toLowerCase()=="img");
			}
			else return false;
		}
		
		return _INHERITED_STYLES.indexOf(prop)==-1;
		if(tag){
			if(tag.toLowerCase()=="img") 
				return prop.toLowerCase().indexOf("margin")!=-1 || _TEXT_PROP.indexOf(prop.toLowerCase())==-1;
				
			if(_TEXT_NODES.indexOf(tag.toLowerCase())!=-1){
				return _TEXT_PROP.indexOf(prop.toLowerCase())!=-1;
			}
			if(_TABLE_NODES.indexOf(tag.toLowerCase())!=-1){
				return _TABLE_PROP.indexOf(prop.toLowerCase())!=-1;
			}
		}
		return _INHERITED_STYLES.indexOf(prop)==-1;
	}
	
	var compositeAttr = function(){
		this.top = 0;
		this.right =0;
		this.bottom =0;
		this.left =0;
		this.addValue = function(p,v)
		{
			if(p.indexOf("top")!=-1) this.top = v
			else if(p.indexOf("right")!=-1) this.right = v;
			else if(p.indexOf("bottom")!=-1) this.bottom = v;
			else if(p.indexOf("left")!=-1) this.left = v;
		}
		
		this.toString = function(){
			if (parseInt(this.top,0)+parseInt(this.right,0) + parseInt(this.bottom,0) + parseInt(this.left,0)==0) return "0";
			else return this.top + " " + this.right + " " +this.bottom + " " + this.left
		}
	}

	
	var _patchStyles = function(e, styleParent, level) {


		var patch = ' style="';
		var spatch = '';
        var style = [];
		var styles = [];
		var margin = new compositeAttr();
        if(document.documentElement.contains(e)) {
            style = window.getComputedStyle(e);
        } else if(e.style.cssText) {
            style = e.style;
        } else {
            return '';
        }
		if(e.tagName){
			if (_TEXT_NODES.indexOf(e.tagName.toLowerCase())!=-1) styles = _textStyles;
			else if(_TABLE_NODES.indexOf(e.tagName.toLowerCase())!=-1) styles = _tableStyles;
			else if ("img"==e.tagName.toLowerCase()) styles = _imgStyles;
			else styles = _requiredStyles;
		}
		
        for (var i = styles.length - 1; i >= 0; --i) {
            try {
				if(/*true || */level==0 ||_canPatch(e.tagName, styles[i].property)){
					var pValue = style[styles[i].property];
					if (pValue && pValue !== styles[i].nullValue && pValue.length !== 0) {
						if(styles[i].property=="text-decoration" && pValue.indexOf("none")>-1){
						} else if(styles[i].property.indexOf("margin-")!=-1){
							margin.addValue(styles[i].property, pValue);
						}
						else
						patch += styles[i].property + ':' + pValue + ';';
					}
				}
            } catch (e) {
                continue;
            }
        }
		patch += "margin:" + margin.toString()+";";
        return patch += '" ';
		
    }
	
	var _getArticle = function(node,isArticle, parentStyle, level) {


        isArticle = isArticle || false;
		if(!parentStyle) parentStyle = new styleCollection();
		if(typeof level == "undefined") level=0; else level++;





        var documentString = '';
        if (node) {
            if (node.tagName && (node.tagName.toUpperCase() == 'script'.toUpperCase() || node.tagName.toUpperCase() == 'style'.toUpperCase())) {} else if (node.nodeType == 3/* || node.nodeType == 8*/) {
                documentString += htmlEncode(node.textContent);
            } else if (_isNodeVisible(node)) {
                var nodeName = node.tagName.toUpperCase();
                if (nodeName == "BODY") {
                    nodeName = "div";
                } else if(nodeName === 'NOSCRIPT' || nodeName === 'NOINDEX') {
                    return '';
                }

                if(node.href && (node.href.indexOf('direct.yandex.ru') > -1 || node.href.indexOf('facebook.com/plugins/like.php')>-1)) {
                    return '';
                }
				
				if(node.id && (node.id=="vk_like" || node.id.indexOf('twitter-widget')>-1)){
					return '';
				}

                switch (nodeName)
                {

                    case "SVG":
                        documentString += _getSVG(node);
                        break;
                    case "BR":
                        try {
                            var nodeStylesString = '';
							if(fvdSaveCss){
								if(isArticle) {
									nodeStylesString = _getStyleForArticleElement(node, _articleStyles);
								} else {
									/*nodeStylesString = _patchStyles(node, _requiredStyles);*/
								}
							}
                        } catch(e){
                            //window.TextSelection.jsError("_parceNode - " +e);
                            var nodeStylesString = '';
                        }
                        return '<br '+nodeStylesString+'/>';

                    default:
                        try {
                            var nodeStylesString = '';
							if(fvdSaveCss){
								if(isArticle) {
									nodeStylesString = _getStyleForArticleElement(node, _articleStyles);
								} else {
									nodeStylesString = _patchStyles(node, parentStyle, level);
								}
							}
                        } catch(e){
                            //window.TextSelection.jsError("_parceNode - " +e);
                            var nodeStylesString = '';
                        }
                        try {
                            var nodeAttributes = _getAttributes(node);
                            var _replacedNodeName =_replaceNodeName(nodeName)
                            documentString += '<' + _replacedNodeName + nodeAttributes  +nodeStylesString+ ' >';
                        } catch(e){
                            //window.TextSelection.jsError("_parceNode - " +e);
                            documentString = '';
                        }
                        if (node.childNodes.length > 0) {
                            for (var i = 0; i < node.childNodes.length; i++) {
                                var child = node.childNodes[i];
                                 documentString += _getArticle(child, isArticle,parentStyle, level);
                            }
                        }
                        documentString += '</' + _replacedNodeName + '>';
                }


            }
        }
        return documentString;
	};


	
	
	var _patcheNodes = function(node,isArticle) {
		//fvdSaveCss = true;
        isArticle = isArticle || false;
        var documentString = '';
        if (node /*&& !_isNimbus(node)*/) {
            if (node.tagName && (node.tagName.toUpperCase() == 'script'.toUpperCase() || node.tagName.toUpperCase() == 'style'.toUpperCase())) {} else if (node.nodeType == 3 || node.nodeType == 8) {
                documentString += htmlEncode(node.textContent);
            } else if (_isNodeVisible(node)) {
                var nodeName = node.tagName.toUpperCase();
                if (nodeName == "BODY") {
                    nodeName = "div";
                } else if(nodeName === 'NOSCRIPT' || nodeName === 'NOINDEX') {
                    return '';
                }

                if(node.href && node.href.indexOf('direct.yandex.ru') > -1) {
                    return ;
                }
						try {
							var nodeStylesString = '';
							if(fvdSaveCss){
								if(isArticle) {
									nodeStylesString = _getStyleForArticleElement(node, _articleStyles);
								} else {
									nodeStylesString = _patchStyles(node);
								}
							}
							node.style.cssText=nodeStylesString;
                        } catch(e){
                            window.TextSelection.jsError("_parceNode - " +e);
                            var nodeStylesString = '';
                        }
						
                        if (node.childNodes.length > 0) {
                            for (var i = 0; i < node.childNodes.length; i++) {
                                var child = node.childNodes[i];
                                 _patcheNodes(child, isArticle);
                            }
                        }


            }
        }

	};
	
	var fixImagePaths = function(ele){
		var elements = ele.getElementsByTagName('img');
		var path;
        for (var i = 0; i < elements.length; i++) {
        	path=elements[i].src;
        	elements[i].src=path;
			window.TextSelection.jsError("fixImagePaths - " +elements[i].src);
        }
	};
	
	var getSelectionBoundaryElement = function(isStart) {
    var range, sel, container;
    if (document.selection) {
        range = document.selection.createRange();
        range.collapse(isStart);
        return range.parentElement();
    } else {
        sel = window.getSelection();
        if (sel.getRangeAt) {
            if (sel.rangeCount > 0) {
                range = sel.getRangeAt(0);
            }
        } else {
            // Old WebKit
            range = document.createRange();
            range.setStart(sel.anchorNode, sel.anchorOffset);
            range.setEnd(sel.focusNode, sel.focusOffset);

            // Handle the case when the selection was selected backwards (from the end to the start in the document)
            if (range.collapsed !== sel.isCollapsed) {
                range.setStart(sel.focusNode, sel.focusOffset);
                range.setEnd(sel.anchorNode, sel.anchorOffset);
            }
       }

        if (range) {
           container = range[isStart ? "startContainer" : "endContainer"];

           // Check if the container is a text node and return its parent if so
           return container.nodeType === 3 ? container.parentNode : container;
        }   
    }
}
	
	var getSelectedHTML = function() {
		if(_border.node){
			return _getArticle(_border.node,false);
		}
		else return "";
	}
	
	var selectionChanged = function(){
		try{
			if(_border.node){
				var text = getSelectedHTML();
				
				_border.rect = defaultMeasureFrameAdapter.getBorderRect(_border.node);
				var handleBounds="";
				if(_border.rect){
					handleBounds = "{'left': " +( document.body.scrollLeft + _border.rect.position.x) + ", ";
			     	handleBounds += "'top': " + (document.body.scrollTop + _border.rect.position.y) + ", ";
					handleBounds += "'right': " + (document.body.scrollLeft + _border.rect.position.x + _border.rect.width) + ", ";
					handleBounds += "'bottom': " + (document.body.scrollTop + _border.rect.position.y + _border.rect.height) + ", 'w':"+document.body.clientWidth+ "}";
				}
				window.TextSelection.setContentWidth(document.body.clientWidth);
				window.TextSelection.selectionChanged("action:selectionChanged", text, handleBounds, null);
			}
		}
		catch(err){
			window.TextSelection.jsError("selectionChanged - " +err);
		}
	};
	
	var getRange = function() {
		return "hello";
    }
	
	this.lastTouchPointString = function(){
		if(lastTouchPoint == null) return "undefined";
		return "{" + lastTouchPoint.x + "," + lastTouchPoint.y + "}";
	};	
	
	this.saveSelectionStart = function(){
		try{
			var sel = window.getSelection();
			var range = sel.getRangeAt(0);
			var saveRange = document.createRange();
			saveRange.setStart(range.startContainer, range.startOffset);
			selectionStartRange = saveRange;
		}
		catch(err){
			window.TextSelection.jsError("saveSelectionStart - " + err);
		}
	};
	
	this.saveSelectionEnd = function(){
		try{
			var sel = window.getSelection();
			var range = sel.getRangeAt(0);
			var saveRange = document.createRange();
			saveRange.setStart(range.endContainer, range.endOffset);
			selectionEndRange = saveRange;
		}
		catch(err){
			window.TextSelection.jsError("saveSelectionEnd - " + err);
		}
	};
	
	this.setStartPos = function(x, y){
		try{
			selectionStartRange = document.caretRangeFromPoint(x, y);
			this.selectBetweenHandles();
		}
		catch(err){
			window.TextSelection.jsError("setStartPos - " + err);
		}
	};
	
	this.setEndPos = function(x, y){
		try{	
			selectionEndRange = document.caretRangeFromPoint(x, y);
			this.selectBetweenHandles();
		}
		catch(err){
			window.TextSelection.jsError("setEndPos - " + err);
		}
	};
	
	var assignStylesToElements = function(ele){
				function __applyCssRuleToElements( rule ){
					var elems = ele.querySelectorAll( rule.selectorText );
					if( !elems ){
						return;
					}
					for( var i = 0; i != elems.length; i++ ){
						var elem = elems[i];
						var styleText = elem.getAttribute("style");
						if( !styleText ){
							styleText = "";
						}
						else{
							if( styleText.lastIndexOf(";") != styleText.length - 1 ){
								styleText += ";";
							}
						}
						styleText += rule.style.cssText;
						elem.setAttribute("style", fvdSaveCss?styleText:"");
					}
				}
				var sheets = document.styleSheets;
				for( var i = 0; i != sheets.length; i++ ){
					var sheet = sheets[i];
					var rules = sheet.cssRules;
					if(rules)
					for( var j = 0; j != rules.length; j++ ){
						var rule = rules[j];
						__applyCssRuleToElements( rule );
					}
				}
	};
	
	this.selectBetweenHandles = function(){
		try{
			var startCaret = selectionStartRange;
			var endCaret = selectionEndRange;
			if (startCaret && endCaret) {
					if(startCaret.compareBoundaryPoints (Range.START_TO_END, endCaret) > 0){
						var temp = startCaret;
						startCaret = endCaret;
						endCaret = temp;
						selectionStartRange = startCaret;
						selectionEndRange = endCaret;
					}
					var range = document.createRange();
					range.setStart(startCaret.startContainer, startCaret.startOffset);
					range.setEnd(endCaret.startContainer, endCaret.startOffset);
					this.clearSelection();
					var selection = window.getSelection();
					selection.addRange(range);
			}
			selectionChanged();
		}
		catch(err){
			window.TextSelection.jsError("selectBetweenHandles - " + err);
		}
	};
	
	var copyItems = function(arrayTo, arrayFrom) {
    if (arrayFrom) {
        for (var item in arrayFrom) {
            arrayTo[item] = arrayFrom[item];
        }
    }
	};
	
	var getElement = function(query, element) {
		return (element || document).querySelector(query);
	};
	
	var proccessHtml = function(arg) {
    if (arg.htmlText || arg.attributes) {
        var at = arg.attributes || {};
        if (arg.htmlText) {
            at.innerHTML = arg.htmlText;
        }
    }

    arg.htmlElement = arg.htmlElement || 'DIV';
    var _object = arg.htmlElement = arg.clone && arg.clone.cloneNode(!0) || (typeof arg.htmlElement == 'string' ? document.createElement(arg.htmlElement) : arg.htmlElement);
    var i;

    if (_object && (!arg.q || arg.q && (arg.dQ = arg.q instanceof Array ? getElement(arg.q[0], arg.q[1]) : getElement(arg.q)))) {
        if (arg.className)
            _object.className = arg.className;
        if (arg.style) {
            copyItems(_object.style, arg.style);
        }
        if (at)
            for (i in at) {
                if (i == 'innerHTML')
                    _object[i] = at[i];
                else
                    _object.setAttribute(i, at[i]);
            }
        if (arg.events)
            for (i in arg.events)
                _object.addEventListener(i, arg.events[i], !1);
        if (arg.appendTo) {
            arg.appendTo.appendChild(_object);
        }
    }
		return _object;
	};
	
	var parents = function(className, elem) {
		/*for (var el = elem; el != null && !RegExp(className).test(el.className); el = el.parentNode);
		return el;*/
		return null;
	};
	
	
	var log = function(text){
		window.TextSelection.jsError(text);
	};
	
	var getHeight = function(){
		var body = document.body,
		html = document.documentElement;
		return Math.max( body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight );
	}
	
	
	
	
	
	this.onTouchStart=function(e){
		//alert(e.target);
		if(this.canClip){
		   cTarget = e.target;
			if(cTarget){
				this.canClip = false;
				if (true) {
					_border.node = cTarget;
					selectionChanged();
				}
			}
			
		}
	}
	
	this.onScroll=function(){
		/*var node = _border.node;
			if (!parents('nimbus_measure', node)&&!parents('nimbus_line', node)) {
			}*/
	}

	this.init = function(){
		this.article=Article;
		
		
		try{
				document.body.addEventListener("touchstart", function(e){
					clearTimeout(t);
					cTarget = e.target;
					lastTouchPoint = {'x': e.changedTouches[0].pageX, 'y':e.changedTouches[0].pageY};
					self.onTouchStart(e);
				
				}, false);
			
				document.addEventListener("touchend", function(){
					t=window.setTimeout(function(){
						if(!self.hasSelection()){
							window.TextSelection.selectionChanged(null, null, null, null);
						}
						else {
						}	
					},500);
				}, false);	
				
				window.TextSelection.Injected();	
		}
		catch(err){
			window.TextSelection.jsError("init - " + err);
		}
			
		
	};

	this.init();
}




