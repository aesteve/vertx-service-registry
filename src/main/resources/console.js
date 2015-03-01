var stdout = java.lang.System.out;
var stderr = java.lang.System.err;
console = {

  /**
   * Log the msg to STDOUT.
   *
   * @param {string} msg The message to log to standard out.
   */
  log: function(msg) {
    stdout.println(msg);
  },

  /**
   * Log the msg to STDERR
   *
   * @param {string} msg The message to log with a warning to standard error.
   */
  warn: function(msg) {
    stderr.println(msg);
  },

  /**
   * Log the msg to STDERR
   *
   * @param {string} msg The message to log with a warning alert to standard error.
   */
  error: function(msg) {
    stderr.println(msg);
  }
};