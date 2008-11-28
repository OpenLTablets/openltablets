var PopupMenu = {
	showChild: function (id, show)
	{
		document.getElementById(id).style.display = show ? "inline" : "none";
	},

	menu_ie: !!(window.attachEvent && !window.opera),
	menu_ns6: document.getElementById && !document.all,
	menuON: false,
	_te_menu : undefined,
	delayedFunction: undefined,
	disappearFunction: undefined,
	disappearInterval1: 5000,
	disappearInterval2: 1000,
	delayedState: {
		extraClass: undefined,
		evt: {},
		contentElement: undefined
	},
    lastTarget: null,
    

    getWindowSize: function () {
		var myWidth = 0, myHeight = 0;
		if (typeof( window.innerWidth ) == 'number') {
			//Non-IE
			myWidth = window.innerWidth;
			myHeight = window.innerHeight;
		} else if (document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight )) {
			//IE 6+ in 'standards compliant mode'
			myWidth = document.documentElement.clientWidth;
			myHeight = document.documentElement.clientHeight;
		} else if (document.body && ( document.body.clientWidth || document.body.clientHeight )) {
			//IE 4 compatible
			myWidth = document.body.clientWidth;
			myHeight = document.body.clientHeight;
		}
		return [myWidth, myHeight];
	},
	
	getScrollXY: function () {
		var scrOfX = 0, scrOfY = 0;
		if (typeof( window.pageYOffset ) == 'number') {
			//Netscape compliant
			scrOfY = window.pageYOffset;
			scrOfX = window.pageXOffset;
		} else if (document.body && ( document.body.scrollLeft || document.body.scrollTop )) {
			//DOM compliant
			scrOfY = document.body.scrollTop;
			scrOfX = document.body.scrollLeft;
		} else if (document.documentElement && ( document.documentElement.scrollLeft || document.documentElement.scrollTop )) {
			//IE6 standards compliant mode
			scrOfY = document.documentElement.scrollTop;
			scrOfX = document.documentElement.scrollLeft;
		}
		return [ scrOfX, scrOfY ];
	},

	_showPopupMenu: function (contentElement, event, extraClass) {
		this.cancelDisappear();

		var scrollXY = this.getScrollXY();
		var windowSizeXY = this.getWindowSize();

		this._te_menu.style.visibility = "hidden";
		this._te_menu.innerHTML = document.getElementById(contentElement).innerHTML;
		this._te_menu.style.display = "inline";
		var divWidth = this._te_menu.clientWidth;
		var divHeight = this._te_menu.clientHeight;

		var posX = event.clientX + 5; var delta = 25;
		if (posX + delta + divWidth > windowSizeXY[0]) posX = windowSizeXY[0] - delta - divWidth;
		if (posX < 0) posX = 0;
		var posY = event.clientY + 5; delta = 5;
		if ( (window.opera && document.body.scrollWidth > windowSizeXY[0])
				  || (window.scrollMaxX && window.scrollMaxX > 0))
			delta = 25;

		if (posY + delta + divHeight > windowSizeXY[1]) posY = event.clientY - 5 - divHeight;
		if (posY < 0) posY = windowSizeXY[1] - delta - divHeight;

		posX += scrollXY[0];posY += scrollXY[1];
		if (this.menu_ns6) {
			this._te_menu.style.left = posX + "px";
			this._te_menu.style.top = posY + "px";
		} else {
			this._te_menu.style.pixelLeft = posX;
			this._te_menu.style.pixelTop = posY;
		}
		if (extraClass)
			this._te_menu.className = "_te_menu " + extraClass;
		else
			this._te_menu.className = "_te_menu";

		this._te_menu.style.visibility = "visible";
		this.menuON = true;
		this.disappearFunction = setTimeout("PopupMenu.closeMenu()", this.disappearInterval1);

        this.lastTarget = this.delayedState.evt.target || this.delayedState.evt.srcElement;
    },

	cancelDisappear : function() {
		if (this.disappearFunction) clearTimeout(this.disappearFunction);
		this.disappearFunction = undefined;
	},

	closeMenu: function () {
		this.cancelDisappear();
		if (this.menuON) {
			this._te_menu.style.display = "none";
		}
	},

	inMenuDiv: function (el) {
		if (el == undefined) return false;
		if (el == this._te_menu) return true;
		if (el.tagName && el.tagName.toLowerCase() == 'a') return false;
		return this.inMenuDiv(el.parentNode);
	},

	getTarget: function (e) {
		var evt = this.menu_ie ? window.event : e;
		var el = undefined;
		if (evt.target) {
			return evt.target;
		} else if (evt.srcElement) {
			return evt.srcElement;
		}
		;
		return undefined;
	},

	_init: function (contentElement, event, extraClass) {
		document.onclick = function(e) {
			var el = PopupMenu.getTarget(e);
			if (el && (el.name != 'menurevealbutton') && !PopupMenu.inMenuDiv(el))
				PopupMenu.closeMenu();
			return true;
		}

		try {
			this._te_menu = document.createElement('<div id="divmenu" class="_te_menu" style="display:none; float:none;z-index:5; position:absolute;">');
		} catch (e) {
			this._te_menu = document.createElement("div");
			this._te_menu.setAttribute("class", "_te_menu");
			this._te_menu.setAttribute("id", "divmenu");
			this._te_menu.style.display = "none";
			this._te_menu.style.cssFloat = "none";
			this._te_menu.style.zIndex = "5";
			this._te_menu.style.position = "absolute";
		}

		this._te_menu.onmouseout = function(e) {
			if (PopupMenu.getTarget(e) == PopupMenu._te_menu) {
				PopupMenu.cancelDisappear();
				PopupMenu.disappearFunction = setTimeout("PopupMenu.closeMenu()", PopupMenu.disappearInterval2);
			}
		}
		this._te_menu.onmouseover = function(e) {
			PopupMenu.cancelDisappear();
		}

		document.body.appendChild(this._te_menu);
		this.showPopupMenu = this._showPopupMenu;
		this.sheduleShowMenu = this._sheduleShowMenu;
	},

	cancelShowMenu: function() {
		if (this.delayedFunction) clearTimeout(this.delayedFunction);
		this.delayedFunction = undefined;
	},

	showAfterDelay : function() {
		this._te_menu.style.display = "none";
		this._showPopupMenu(this.delayedState.contentElement, this.delayedState.evt, this.delayedState.extraClass);
    },

	_sheduleShowMenu: function(contentElement, event, delay, extraClass) {
		this.cancelShowMenu();
		this.delayedState.evt.clientX = event.clientX;
		this.delayedState.evt.clientY = event.clientY;
		this.delayedState.evt.target = event.target ? event.target : undefined;
		this.delayedState.evt.srcElement = event.srcElement ? event.srcElement : undefined;
		this.delayedState.extraClass = extraClass;
		this.delayedState.contentElement = contentElement;

		this.delayedFunction = setTimeout("PopupMenu.showAfterDelay()", delay);
	},

	// init
	showPopupMenu: function() {this._init(); this._showPopupMenu.apply(this, arguments);},
	sheduleShowMenu: function() {this._init(); this._sheduleShowMenu.apply(this, arguments);}
}