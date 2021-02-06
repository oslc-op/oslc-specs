var specConfig = {
  status: "WD",
  revision: "02",
  publishDate: null,
  previousPublishDate: "2020-09-17",
  previousMaturity: "PS",
};

var wdBase = "https://oslc-op.github.io/oslc-specs/specs/core/";
var forceWdBase = false; // set to true if there are problems with the URIs
var oasisBase = "https://docs.oasis-open-projects.org/oslc-op/core/v3.0/";

var thisBase = wdBase;
if (specConfig.status != "WD") {
  thisBase = oasisBase + specConfig.status.toLowerCase() + specConfig.revision + "/";
} else {
  // if it's a WD, set
  var scriptUri = document.currentScript.src;
  var scriptBase = scriptUri.substr(0, scriptUri.lastIndexOf("/") + 1);
  if (!forceWdBase) {
    var thisBase = scriptBase;
  }
}
