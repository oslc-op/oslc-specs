var specConfig = {
  status: "OS",
  // revision: "02",
  publishDate: "2021-08-26T12:00Z",
  previousPublishDate: null,
  previousMaturity: "PS",
};

var wdBase = "https://oslc-op.github.io/oslc-specs/specs/core/";
var forceWdBase = false; // set to true if there are problems with the URIs
var oasisBase = "https://docs.oasis-open-projects.org/oslc-op/core/v3.0/";

var thisBase = wdBase;
if (specConfig.status != "WD") {
  thisBase = oasisBase + specConfig.status.toLowerCase();
  if(specConfig.status != "OS") {
    thisBase += specConfig.revision;
  }
  thisBase +="/";
} else {
  // if it's a WD, set
  var scriptUri = document.currentScript.src;
  var scriptBase = scriptUri.substr(0, scriptUri.lastIndexOf("/") + 1);
  if (!forceWdBase) {
    var thisBase = scriptBase;
  }
}
