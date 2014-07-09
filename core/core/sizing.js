var pages;
var pageIndex = -1;

function loadPages()
{
	var topMenu = document.getElementById("topMenu");
	var menuItems = topMenu.getElementsByClassName("size-item");
	
	pages = [];
	
	for (var i = 0, pi = 0; i < menuItems.length; i++)
	{
		var sizeText = menuItems[i].getAttribute("data-size");
		
		if (sizeText !== "whatfits")
		{
			var size = parseInt(sizeText);
			var pageName = menuItems[i].getAttribute("data-page");
			
			pages.push(
				{
					size: size,
					name: pageName
				});
			
			if (window.location.pathname.indexOf(pageName) >= 0)
				pageIndex = pi;
			
			pi++;
		}
	}

	pages.sort(function(page1, page2)
	{
		return page1.size < page2.size ? -1 : page1.size > page2.size ? 1 : 0;
	});
}

var allImagesLoaded = false;

function watchImages()
{
	var allImages = imagesLoaded(document.body);
	
	function allLoaded()
	{
		allImagesLoaded = true;
		brighten(document.getElementsByClassName("story-coda")[0]);
	}
	
	allImages.on("always", allLoaded);
	
	var images = document.getElementsByTagName("img");
	var imageArray = [];
	var loaded = [];
	var brightenedUpTo = 0;
	
	for (var i = 0; i < images.length; i++)
	{
		var image = images[i];
		imageArray.push(image);
		loaded.push(false);
		
		(function()
		{
			var i_ = i;
			imagesLoaded(image, function()
				{
					loaded[i_] = true;
					tryToBrightenMoreImages();
				});
		})();
	}
	
	function tryToBrightenMoreImages()
	{
		for (; brightenedUpTo < loaded.length && loaded[brightenedUpTo]; brightenedUpTo++)
		{
			var image = imageArray[brightenedUpTo];
			
			var element = image;
			while (element.className.indexOf("story-item") < 0 && element.parentNode)
				element = element.parentNode;
			
			if (element)
			{
				var caption = element.getElementsByClassName("caption")[0];
				brighten(caption);
			}
		}
	}
}

function brighten(storyTextElement)
{
	if (storyTextElement)
		storyTextElement.style.color = "inherit";
}

function configureMenu()
{
	var topMenu = document.getElementById("topMenu");
	var rightMenu = document.getElementById("rightMenu");
	var menuNub = document.getElementById("menuNub");
	
	topMenu.getElementsByClassName("size-menu-x")[0].onclick = 
	rightMenu.getElementsByClassName("size-menu-x")[0].onclick =
		function()
		{
			setMenuState(false);
		}
	
	menuNub.onclick = function()
		{
			setMenuState(true);
		}
	
	configureMenuItems(topMenu);
	configureMenuItems(rightMenu);
}

function configureMenuItems(menu)
{
	var menuItems = menu.getElementsByClassName("size-item");
	
	for (var i = 0; i < menuItems.length; i++)
	{
		var item = menuItems[i];
		item.onclick = function(event)
			{
				event = event || window.event;
				var target = event.target || event.srcElement;
				setSize(target.getAttribute("data-size"));
			};
	}
}

var hasLoaded = false;

window.onload = function()
{
	loadPages();
	watchImages();
	configureMenu();
	update();
	hasLoaded = true;
}

window.onresize = function()
{
	if (hasLoaded)
		update();
}

function update()
{
	updateMenus();
	goToCurrentSize();
}

function updateMenus()
{
	var topMenu = document.getElementById("topMenu");
	var rightMenu = document.getElementById("rightMenu");
	var menuNub = document.getElementById("menuNub");
	
	var items1 = topMenu.getElementsByClassName("size-item");
	var items2 = rightMenu.getElementsByClassName("size-item");
	var items = [];
	
	for (var i = 0; i < items1.length; i++) items.push(items1[i]);
	for (var i = 0; i < items2.length; i++) items.push(items2[i]);
	
	var sizeCount = 0;
	var currentSize = getCurrentSize();
	
	for (var i = 0; i < items.length; i++)
	{
		var itemSize = items[i].getAttribute("data-size");
		if (!isNaN(itemSize)) sizeCount++;
		
		if (currentSize.matchesText(itemSize))
			addClass(items[i], "selected");
		else
			removeClass(items[i], "selected");
	}
	
	if (sizeCount)
		if (menuState())
		{
			setVisibility(menuNub, false);
			
			var width = document.documentElement.clientWidth;
			var height = document.documentElement.clientHeight;
			var ratio = width / height;
			
			if (ratio > 1.3)
			{
				setVisibility(rightMenu, true);
				setRightPadding(true);
				setVisibility(topMenu, false);
			}
			else
			{
				setVisibility(rightMenu, false);
				setRightPadding(false);
				setVisibility(topMenu, true);
			}
		}
		else
		{
			setVisibility(rightMenu, false);
			setRightPadding(false);
			setVisibility(topMenu, false);
			setVisibility(menuNub, true);
		}
	else
	{
		setVisibility(rightMenu, false);
		setRightPadding(false);
		setVisibility(topMenu, false);
		setVisibility(menuNub, false);
	}
}

function addClass(element, className)
{
	if (!element.className.indexOf(className) >= 0)
		element.className = element.className + " " + className;
}

function removeClass(element, className)
{
	var startIndex = element.className.indexOf(className);
	
	if (startIndex >= 0)
		element.className = element.className.substring(0, startIndex) + element.className.substring(startIndex + className.length + 1);
}

function setVisibility(element, value)
{
	if (value)
		element.style.display = "inherit";
	else
		element.style.display = "none";
}

function setRightPadding(on)
{
	var container = document.getElementsByClassName("main-container")[0];
	var rightMenu = document.getElementById("rightMenu");
	
	if (on)
		container.style.paddingRight = "" + rightMenu.offsetWidth + "px";
	else
		container.style.paddingRight = "0";
}

function menuState()
{
	return !sessionStorage.getItem("menuIsClosed");
}

function setMenuState(value)
{
	sessionStorage.setItem("menuIsClosed", value ? "" : "true");
	update();
}

function availableWidth()
{
	var availableWidth = document.documentElement.clientWidth;
	
	var rightMenu = document.getElementById("rightMenu");
	
	if (rightMenu.getAttribute("display") !== "none")
		availableWidth -= rightMenu.offsetWidth;
	
	return availableWidth;
}

function goToCurrentSize()
{
	var size = getCurrentSize();
	var targetPageIndex = size.getTargetPageIndex();
	
	if (pageIndex !== targetPageIndex)
	{
		var anchorName = getSelectedAnchor();
		window.location.assign(pages[targetPageIndex].name + (anchorName ? "#" + anchorName : ""));
	}
}

function getSelectedAnchor()
{
	if (allImagesLoaded)
	{
		var anchors = document.getElementsByTagName("a");
		
		for (var i = 0; i < anchors.length; i++)
			if (anchors[i].getBoundingClientRect().top >= 0)
				return anchors[i].getAttribute("id");
		
		return null;
	}
	else
	{
		var hash = window.location.hash;
		
		if (hash)
			return hash.substring(1);
		else
			return null;
	}
}

function setSize(sizeInt)
{
	new Size(sizeInt).store();
	update();
}

function Size(int)
{
	if (int !== null && typeof(int) !== "undefined" && int !== "whatfits")
		this.value = parseInt("" + int);
	else
		this.value = "whatfits";
	
	this.store = function()
	{
		sessionStorage.setItem("currentSize", this.value);
	};
	
	this.getTargetPageIndex = function()
	{
		for (var i = pages.length - 1; i >= 0; i--)
			if (this.value === "whatfits")
			{
				if (pages[i].size <= availableWidth())
					return i;
			}
			else
				if (pages[i].size === this.value)
					return i;
		
		return 0;
	};
	
	this.matchesText = function(text)
	{
		return "" + this.value === text;
	}
}

function getCurrentSize()
{
	var rawSize = sessionStorage.getItem("currentSize");
	
	if (rawSize === "whatfits" || !rawSize)
		return new Size(null);
	else
		return new Size(parseInt(rawSize));
}
