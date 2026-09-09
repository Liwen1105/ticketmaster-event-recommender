(function() {

  /**
   * Variables
   */
  var user_id = '1111';
  var user_fullname = '1111';
  var lng = -122.08;
  var lat = 37.38;

  /**
   * Initialize major event handlers
   */
  function init() {
    // register event listeners
    document.querySelector('#login-form-btn').addEventListener('click', onSessionInvalid);
    document.querySelector('#login-btn').addEventListener('click', login);
    document.querySelector('#register-form-btn').addEventListener('click', showRegisterForm);
    document.querySelector('#register-btn').addEventListener('click', register);
    document.querySelector('#nearby-btn').addEventListener('click', loadNearbyevents);
    document.querySelector('#fav-btn').addEventListener('click', loadFavoriteevents);
    document.querySelector('#recommend-btn').addEventListener('click', loadRecommendedevents);
    validateSession();
    // onSessionValid({"user_id":"1111","name":"John Smith","status":"OK"});
  }

  /**
   * Session
   */
  function validateSession() {
    onSessionInvalid();
    // The request parameters
    var url = './login';
    var req = JSON.stringify({});

    // display loading message
    showLoadingMessage('Validating session...');

    // make AJAX call
    ajax('GET', url, req,
      // session is still valid
      function(res) {
        var result = JSON.parse(res);

        if (result.status === 'OK') {
          onSessionValid(result);
        }
      });
  }

  function onSessionValid(result) {
    user_id = result.user_id;
    user_fullname = result.name;

    var loginForm = document.querySelector('#login-form');
    var registerForm = document.querySelector('#register-form');
    var eventNav = document.querySelector('#event-nav');
    var eventList = document.querySelector('#event-list');
    var avatar = document.querySelector('#avatar');
    var welcomeMsg = document.querySelector('#welcome-msg');
    var logoutBtn = document.querySelector('#logout-link');

    welcomeMsg.innerHTML = 'Welcome, ' + user_fullname;

    showElement(eventNav);
    showElement(eventList);
    showElement(avatar);
    showElement(welcomeMsg);
    showElement(logoutBtn, 'inline-block');
    hideElement(loginForm);
    hideElement(registerForm);

    initGeoLocation();
  }

  function onSessionInvalid() {
    var loginForm = document.querySelector('#login-form');
    var registerForm = document.querySelector('#register-form');
    var eventNav = document.querySelector('#event-nav');
    var eventList = document.querySelector('#event-list');
    var avatar = document.querySelector('#avatar');
    var welcomeMsg = document.querySelector('#welcome-msg');
    var logoutBtn = document.querySelector('#logout-link');

    hideElement(eventNav);
    hideElement(eventList);
    hideElement(avatar);
    hideElement(logoutBtn);
    hideElement(welcomeMsg);
    hideElement(registerForm);

    clearLoginError();
    showElement(loginForm);
  }

  function hideElement(element) {
    element.style.display = 'none';
  }

  function showElement(element, style) {
    var displayStyle = style ? style : 'block';
    element.style.display = displayStyle;
  }
  
  function showRegisterForm() {
    var loginForm = document.querySelector('#login-form');
    var registerForm = document.querySelector('#register-form');
    var eventNav = document.querySelector('#event-nav');
    var eventList = document.querySelector('#event-list');
    var avatar = document.querySelector('#avatar');
    var welcomeMsg = document.querySelector('#welcome-msg');
    var logoutBtn = document.querySelector('#logout-link');

    hideElement(eventNav);
    hideElement(eventList);
    hideElement(avatar);
    hideElement(logoutBtn);
    hideElement(welcomeMsg);
    hideElement(loginForm);
    
    clearRegisterResult();
    showElement(registerForm);
  }  
  

  function initGeoLocation() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        onPositionUpdated,
        onLoadPositionFailed, {
          maximumAge: 60000
        });
      showLoadingMessage('Retrieving your location...');
    } else {
      onLoadPositionFailed();
    }
  }

  function onPositionUpdated(position) {
    lat = position.coords.latitude;
    lng = position.coords.longitude;

    loadNearbyevents();
  }

  function onLoadPositionFailed() {
    console.warn('navigator.geolocation is not available');
    getLocationFromIP();
  }

  function getLocationFromIP() {
    // get location from http://ipinfo.io/json
    var url = 'http://ipinfo.io/json'
    var data = null;

    ajax('GET', url, data, function(res) {
      var result = JSON.parse(res);
      if ('loc' in result) {
        var loc = result.loc.split(',');
        lat = loc[0];
        lng = loc[1];
      } else {
        console.warn('Getting location by IP failed.');
      }
      loadNearbyevents();
    });
  }

  // -----------------------------------
  // Login
  // -----------------------------------

  function login() {
    var username = document.querySelector('#username').value;
    var password = document.querySelector('#password').value;
    password = md5(username + md5(password));

    // The request parameters
    var url = './login';
    var req = JSON.stringify({
      user_id : username,
      password : password,
    });

    ajax('POST', url, req,
      // successful callback
      function(res) {
        var result = JSON.parse(res);

        // successfully logged in
        if (result.status === 'OK') {
          onSessionValid(result);
        }
      },

      // error
      function() {
        showLoginError();
      },
      true);
  }

  function showLoginError() {
    document.querySelector('#login-error').innerHTML = 'Invalid username or password';
  }

  function clearLoginError() {
    document.querySelector('#login-error').innerHTML = '';
  }

  // -----------------------------------
  // Register
  // -----------------------------------

  function register() {
    var username = document.querySelector('#register-username').value;
    var password = document.querySelector('#register-password').value;
    var firstName = document.querySelector('#register-first-name').value;
    var lastName = document.querySelector('#register-last-name').value;
    
    if (username === "" || password == "" || firstName === "" || lastName === "") {
    	showRegisterResult('Please fill in all fields');
    	return
    }
    
    if (username.match(/^[a-z0-9_]+$/) === null) {
    	showRegisterResult('Invalid username');
    	return
    }
    
    password = md5(username + md5(password));

    // The request parameters
    var url = './register';
    var req = JSON.stringify({
      user_id : username,
      password : password,
      first_name: firstName,
      last_name: lastName,
    });

    ajax('POST', url, req,
      // successful callback
      function(res) {
        var result = JSON.parse(res);

        // successfully logged in
        if (result.status === 'OK') {
        	showRegisterResult('Succesfully registered');
        } else {
        	showRegisterResult('User already existed');
        }
      },

      // error
      function() {
    	  showRegisterResult('Failed to register');
      },
      true);
  }

  function showRegisterResult(registerMessage) {
    document.querySelector('#register-result').innerHTML = registerMessage;
  }

  function clearRegisterResult() {
    document.querySelector('#register-result').innerHTML = '';
  }


  // -----------------------------------
  // Helper Functions
  // -----------------------------------

  /**
   * A helper function that makes a navigation button active
   *
   * @param btnId - The id of the navigation button
   */
  function activeBtn(btnId) {
    var btns = document.querySelectorAll('.main-nav-btn');

    // deactivate all navigation buttons
    for (var i = 0; i < btns.length; i++) {
      btns[i].className = btns[i].className.replace(/\bactive\b/, '');
    }

    // active the one that has id = btnId
    var btn = document.querySelector('#' + btnId);
    btn.className += ' active';
  }

  function showLoadingMessage(msg) {
    var eventList = document.querySelector('#event-list');
    eventList.innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i> ' +
      msg + '</p>';
  }

  function showWarningMessage(msg) {
    var eventList = document.querySelector('#event-list');
    eventList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i> ' +
      msg + '</p>';
  }

  function showErrorMessage(msg) {
    var eventList = document.querySelector('#event-list');
    eventList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i> ' +
      msg + '</p>';
  }

  /**
   * A helper function that creates a DOM element <tag options...>
   * @param tag
   * @param options
   * @returns {Element}
   */
  function $create(tag, options) {
    var element = document.createElement(tag);
    for (var key in options) {
      if (options.hasOwnProperty(key)) {
        element[key] = options[key];
      }
    }
    return element;
  }

  /**
   * AJAX helper
   *
   * @param method - GET|POST|PUT|DELETE
   * @param url - API end point
   * @param data - request payload data
   * @param successCallback - Successful callback function
   * @param errorCallback - Error callback function
   */
  function ajax(method, url, data, successCallback, errorCallback) {
    var xhr = new XMLHttpRequest();

    xhr.open(method, url, true);

    xhr.onload = function() {
      if (xhr.status === 200) {
        successCallback(xhr.responseText);
      } else {
        errorCallback();
      }
    };

    xhr.onerror = function() {
      console.error("The request couldn't be completed.");
      errorCallback();
    };

    if (data === null) {
      xhr.send();
    } else {
      xhr.setRequestHeader("Content-Type",
        "application/json;charset=utf-8");
      xhr.send(data);
    }
  }

  // -------------------------------------
  // AJAX call server-side APIs
  // -------------------------------------

  /**
   * API #1 Load the nearby events API end point: [GET]
   * /search?user_id=1111&lat=37.38&lon=-122.08
   */
  function loadNearbyevents() {
    console.log('loadNearbyevents');
    activeBtn('nearby-btn');

    // The request parameters
    var url = './search';
    var params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;
    var data = null;

    // display loading message
    showLoadingMessage('Loading nearby events...');

    // make AJAX call
    ajax('GET', url + '?' + params, data,
      // successful callback
      function(res) {
        var events = JSON.parse(res);
        if (!events || events.length === 0) {
          showWarningMessage('No nearby event.');
        } else {
          listevents(events);
        }
      },
      // failed callback
      function() {
        showErrorMessage('Cannot load nearby events.');
      }
    );
  }

  /**
   * API #2 Load favorite (or visited) events API end point: [GET]
   * /history?user_id=1111
   */
  function loadFavoriteevents() {
    activeBtn('fav-btn');

    // request parameters
    var url = './history';
    var params = 'user_id=' + user_id;
    var req = JSON.stringify({});

    // display loading message
    showLoadingMessage('Loading favorite events...');

    // make AJAX call
    ajax('GET', url + '?' + params, req, function(res) {
      var events = JSON.parse(res);
      if (!events || events.length === 0) {
        showWarningMessage('No favorite event.');
      } else {
        listevents(events);
      }
    }, function() {
      showErrorMessage('Cannot load favorite events.');
    });
  }

  /**
   * API #3 Load recommended events API end point: [GET]
   * /recommendation?user_id=1111
   */
  function loadRecommendedevents() {
    activeBtn('recommend-btn');

    // request parameters
    var url = './recommendation' + '?' + 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;
    var data = null;

    // display loading message
    showLoadingMessage('Loading recommended events...');

    // make AJAX call
    ajax('GET', url, data,
      // successful callback
      function(res) {
        var events = JSON.parse(res);
        if (!events || events.length === 0) {
          showWarningMessage('No recommended event. Make sure you have favorites.');
        } else {
          listevents(events);
        }
      },
      // failed callback
      function() {
        showErrorMessage('Cannot load recommended events.');
      }
    );
  }

  /**
   * API #4 Toggle favorite (or visited) events
   *
   * @param event_id - The event business id
   *
   * API end point: [POST]/[DELETE] /history request json data: {
   * user_id: 1111, visited: [a_list_of_business_ids] }
   */
  function changeFavoriteevent(event_id) {
    // check whether this event has been visited or not
    var li = document.querySelector('#event-' + event_id);
    var favIcon = document.querySelector('#fav-icon-' + event_id);
    var favorite = !(li.dataset.favorite === 'true');

    // request parameters
    var url = './history';
    var req = JSON.stringify({
      user_id: user_id,
      favorite: [event_id]
    });
    var method = favorite ? 'POST' : 'DELETE';

    ajax(method, url, req,
      // successful callback
      function(res) {
        var result = JSON.parse(res);
        if (result.status === 'OK' || result.result === 'SUCCESS') {
          li.dataset.favorite = favorite;
          favIcon.className = favorite ? 'fa fa-heart' : 'fa fa-heart-o';
        }
      });
  }

  // -------------------------------------
  // Create event list
  // -------------------------------------

  /**
   * List recommendation events base on the data received
   *
   * @param events - An array of event JSON objects
   */
  function listevents(events) {
    var eventList = document.querySelector('#event-list');
    eventList.innerHTML = ''; // clear current results

    for (var i = 0; i < events.length; i++) {
      addevent(eventList, events[i]);
    }
  }

  /**
   * Add a single event to the list
   *
   * @param eventList - The <ul id="event-list"> tag (DOM container)
   * @param event - The event data (JSON object)
   *
   <li class="event">
   <img alt="event image" src="https://s3-media3.fl.yelpcdn.com/bphoto/EmBj4qlyQaGd9Q4oXEhEeQ/ms.jpg" />
   <div>
   <a class="event-name" href="#" target="_blank">event</a>
   <p class="event-category">Vegetarian</p>
   <div class="stars">
   <i class="fa fa-star"></i>
   <i class="fa fa-star"></i>
   <i class="fa fa-star"></i>
   </div>
   </div>
   <p class="event-address">699 Calderon Ave<br/>Mountain View<br/> CA</p>
   <div class="fav-link">
   <i class="fa fa-heart"></i>
   </div>
   </li>
   */
  function addevent(eventList, event) {
    var event_id = event.event_id;

    // create the <li> tag and specify the id and class attributes
    var li = $create('li', {
      id: 'event-' + event_id,
      className: 'event'
    });

    // set the data attribute ex. <li data-event_id="G5vYZ4kxGQVCR" data-favorite="true">
    li.dataset.event_id = event_id;
    li.dataset.favorite = event.favorite;

    // event image
    if (event.image_url) {
      li.appendChild($create('img', { src: event.image_url }));
    } else {
      li.appendChild($create('img', {
        src: 'https://assets-cdn.github.com/images/modules/logos_page/GitHub-Mark.png'
      }));
    }
    // section
    var section = $create('div');

    // title
    var title = $create('a', {
      className: 'event-name',
      href: event.url,
      target: '_blank'
    });
    title.innerHTML = event.name;
    section.appendChild(title);

    // category
    var category = $create('p', {
      className: 'event-category'
    });
    category.innerHTML = 'Category: ' + event.categories.join(', ');
    section.appendChild(category);

    // stars
    var stars = $create('div', {
      className: 'stars'
    });

    for (var i = 0; i < event.rating; i++) {
      var star = $create('i', {
        className: 'fa fa-star'
      });
      stars.appendChild(star);
    }

    if (('' + event.rating).match(/\.5$/)) {
      stars.appendChild($create('i', {
        className: 'fa fa-star-half-o'
      }));
    }

    section.appendChild(stars);

    li.appendChild(section);

    // address
    var address = $create('p', {
      className: 'event-address'
    });

    // ',' => '<br/>',  '\"' => ''
    address.innerHTML = event.address.replace(/,/g, '<br/>').replace(/\"/g, '');
    li.appendChild(address);

    // favorite link
    var favLink = $create('p', {
      className: 'fav-link'
    });

    favLink.onclick = function() {
      changeFavoriteevent(event_id);
    };

    favLink.appendChild($create('i', {
      id: 'fav-icon-' + event_id,
      className: event.favorite ? 'fa fa-heart' : 'fa fa-heart-o'
    }));

    li.appendChild(favLink);
    eventList.appendChild(li);
  }

  init();

})();