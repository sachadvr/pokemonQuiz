var exec = require('cordova/exec');

var vibratePattern = function(pattern) {
    if (!pattern || pattern.length === 0) {
        return;
    }
    exec(null, null, "Vibration", "vibrateWithPattern", [pattern]);
};

var vibrateSimple = function(time) {
    if (typeof time !== 'number' || time < 0) {
        console.warn('Invalid vibration duration. Must be a positive number.');
        return;
    }
    exec(null, null, "Vibration", "vibrate", [time]);
};

var vibrate = function(param) {
    if (typeof param === 'undefined' || param === null) {
        return;
    }

    if (typeof param === 'number') {
        if (param === 0) {
            exec(null, null, "Vibration", "cancelVibration", []);
        } else {
            vibrateSimple(param);
        }
    } else if (Array.isArray(param)) {
        if (param.length === 0) {
            return;
        }

        var isAllZeros = param.every(function(val) {
            return val === 0;
        });

        if (isAllZeros) {
            exec(null, null, "Vibration", "cancelVibration", []);
        } else {
            vibratePattern(param);
        }
    } else {
        console.warn('Invalid parameter type for vibrate. Expected number or array.');
    }
};

module.exports = vibrate;
