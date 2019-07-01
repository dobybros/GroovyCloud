/**
 * 
 */

var Console = (function() {
	var logger = Java.type('chat.logs.LoggerEx');

	_console = function() {}
	_console.prototype.log = function(msg) {
		logger.info("js", msg);
	}
	_console.prototype.info = function(msg) {
		logger.info("js", msg);
	}
	_console.prototype.debug = function(msg) {
		logger.debug("js", msg);
	}
	_console.prototype.warn = function(msg) {
		logger.warn("js", msg);
	}
	_console.prototype.error = function(msg) {
		logger.error("js", msg);
	}
	return _console;
})();

var console = new Console();