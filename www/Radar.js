const cordova = require('cordova');
const exec = (action, args = []) => {
  return new Promise(function (resolve, reject) {
    cordova.exec(resolve, reject, "Radar", action, args);
  });
};

const initialize = (publishableKey) => {
  return exec('initialize', [publishableKey]);
}

const setUserId = (userId) => {
  return exec('setUserId', [userId]);
};

const getUserId = () => {
  return exec("getUserId")
}

const setDescription = (description) => {
  return exec('setDescription', [description]);
};

const getDescription = () => {
  return exec("getDescription");
}

const setMetadata = (metadata) => {
  return exec('setMetadata', [metadata]);
};

const getMetadata = () => {
  return exec('getMetadata');
};

const setAnonymousTrackingEnabled = (enabled) => {
  return exec('setAnonymousTrackingEnabled', [enabled]);
}

const setAdIdEnabled = (enabled) => {
  return exec('setAdIdEnabled', [enabled]);
}

const getPermissionsStatus = () => {
  return exec('getPermissionsStatus');
};

const requestPermissions = (background) => {
  return exec('requestPermissions', [background]);
};

const requestPermissionsSync = (background) => {
  return exec('requestPermissionsSync', [background]);
};

const getLocation = (desiredAccuracy) => {
  return exec('getLocation', [desiredAccuracy]);
};

const trackOnce = (arg1) => {
  return exec('trackOnce', [arg1]);
};

const startTrackingEfficient = () => {
  return exec('startTrackingEfficient');
};

const startTrackingResponsive = () => {
  return exec('startTrackingResponsive');
};

const startTrackingContinuous = () => {
  return exec('startTrackingContinuous');
};

const startTrackingCustom = (options) => {
  return exec('startTrackingCustom', [options]);
};

const mockTracking = (options) => {
  return exec('mockTracking', [options]);
};

const stopTracking = () => {
  return exec('stopTracking');
};

const isTracking = () => {
  return exec('isTracking');
}

const getTrackingOptions = () => {
  return exec('getTrackingOptions');
}

const onEvents = (callback, errorCallback) => {
  cordova.exec(callback, errorCallback, "Radar", "onEvents", []);
};

const onLocation = (callback, errorCallback) => {
  cordova.exec(callback, errorCallback, "Radar", "onLocation", []);
};

const onClientLocation = (callback, errorCallback) => {
  cordova.exec(callback, errorCallback, "Radar", "onClientLocation", []);
};

const onError = (callback, errorCallback) => {
  cordova.exec(callback, errorCallback, "Radar", "onError", []);
};

const offEvents = () => {
  return exec('offEvents');
};

const offLocation = () => {
  return exec('offLocation');
};

const offClientLocation = () => {
  return exec('offClientLocation');
};

const offError = () => {
  return exec('offEvents');
};

const getTripOptions = () => {
  return exec('getTripOptions');
};

const startTrip = (options) => {
  return exec('startTrip', [options]);
};

const updateTrip = (options) => {
  return exec('updateTrip', [options]);
};

const completeTrip = () => {
  return exec('completeTrip');
};

const cancelTrip = () => {
  return exec('cancelTrip');
};

const getContext = (arg1) => {
  return exec('getContext', [arg1]);
};

const searchPlaces = (options) => {
  return exec('searchPlaces', [options]);
};

const searchGeofences = (options) => {
  return exec('searchGeofences', [options]);
};

const autocomplete = (options) => {
  return exec('autocomplete', [options]);
};

const geocode = (query) => {
  return exec('geocode', [query]);
};

const reverseGeocode = (arg1) => {
    return exec('reverseGeocode', [arg1]);
};

const ipGeocode = () => {
  return exec('ipGeocode');
};

const getDistance = (options) => {
  return exec('getDistance', [options]);
};

const getMatrix = (options) => {
  return exec('getMatrix', [options]);
};

const setForegroundServiceOptions = (options) => {
  return exec('setForegroundServiceOptions', [options])
}

const setLogLevel = (logLevel) => {
  return exec('setLogLevel', [logLevel]);
};

const sendEvent = (options) => {
  return exec('sendEvent', [options]);
}

const Radar = {
  initialize,
  setUserId,
  getUserId,
  setDescription,
  getDescription,
  setMetadata,
  getMetadata,
  setAnonymousTrackingEnabled,
  setAdIdEnabled,
  getPermissionsStatus,
  requestPermissions,
  requestPermissionsSync,
  getLocation,
  trackOnce,
  startTrackingEfficient,
  startTrackingResponsive,
  startTrackingContinuous,
  startTrackingCustom,
  mockTracking,
  stopTracking,
  isTracking,
  getTrackingOptions,
  onEvents,
  onLocation,
  onClientLocation,
  onError,
  offEvents,
  offLocation,
  offClientLocation,
  offError,
  getTripOptions,
  startTrip,
  updateTrip,
  completeTrip,
  cancelTrip,
  getContext,
  searchPlaces,
  searchGeofences,
  autocomplete,
  geocode,
  reverseGeocode,
  ipGeocode,
  getDistance,
  getMatrix,
  setForegroundServiceOptions,
  setLogLevel,
  sendEvent
};

module.exports = Radar;
