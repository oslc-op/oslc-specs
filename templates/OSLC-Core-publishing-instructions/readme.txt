NOTE: These instructions apply to the old OASIS TC publishing process. Completely new instructions will be needed for the Open Project.

Jim & Martin,

To help you with preparation of the OSLC Core Committee Spec Draft, I have prepared templates to show you how the cover pages, urls, filenames etc. will need to appear. I know that you are not using MS Office as your editable source so these aren't intended for editorial use. They are just the most straightforward way for me to lay out look and feel and content for you.

The layout, filenames, titles, etc. in these files are all done according to the procedures we have in place for implementing the OASIS TC Process (https://www.oasis-open.org/policies-guidelines/tc-process) and the OASIS Naming Directives (http://docs.oasis-open.org/specGuidelines/ndr/namingDirectives.html).

A. What will be needed for the Committee Spec Draft and its public review

Normally I do the prep and the work is invisible to the TCs. Since you are using ReSpec and doing the editing yourself, I'm giving you instructions for what I normally do.

The TC Process requires that TC work products be published in three formats: editable source, HTML and PDF. In your case, since you are using ReSpec, HTML will serve as both itself and as the editable source. I will still need you to prepare PDFs of each file.

I will need a complete set of files for the Committee Specification Draft (csd) and a complete set for the Public Review Draft (csprd). I have a template included for each of the files in the csd copy. I made a version of the Overview as a csprd in order to show you what will need to change between the two sets of files. I have listed the changes that will be needed below.

Two open questions I have at the moment are (a) what do we need to have in the docs.oasis-open.org folders in order for the HTML to appear properly and (b) what to we need to have in place in order for the Latest version links to work? The Latest version links are stored at the Version level of the folder hierarchy and they are symlinks to the most current version of that file. We've had problems making them with with XML with style sheets, so I won't be surprised if we have to work out a solution to that.

B. What information will be needed on your cover pages?

As you will see, I have organized the files, filenames, folder hierarchy, etc. the way we do for multi-part specifications. This is based on the naming directive formula that organizes the approved work products in docs.oasis-open.org using the following formula:

http://docs.oasis-open.org/<tc abbrev>/<work product abbrev>/v.##/<work stage abbrev>/<part# and short name>/<filename>

So for example, Part 1: Overview will at:

http://docs.oasis-open.org/oslc-core/oslc-core-spec/v3.0/csd01/part1-overview/oslc-core-spec-v3.0-csd01-part1-overview.html

The file OSLC-Core-coverage-metadata.txt lists the titles and links for each part.

For an example of a similar multi-part work published recently and to see its look and feel in HTML, see the TAXII v1.1.1 spec at http://docs.oasis-open.org/cti/taxii/v1.1.1/

NOTE: I prepared this assuming that a work product abbreviation of oslc-core-spec and worked to align things as closely to the names you used as possible. Normally this gets sorted out when a TC requests a template for the work. In any case, if you want to discuss changes to these names, just let me know. I'm happy to work with you on an acceptable solution.

When you prepare your copies, they'll need to look as close to HTML versions of this layout as possible. (Again, see the documents for TAXII for a comparison.)

Taking a spec from the top:

B.1 - the titles. I have restructured your titles to what we require: <title> Version #.#. Part #: <part title>

B.2 - these will be Committee Specification Draft 01. For the public review copies, they will be:

Committee Specification Draft 01 /
Public Review Draft 01

See the section below on changing a csd to a csprd for details.

B.3 - Change DD Month 2016 to the 2 digit date and month when the TC votes to approve the action. So for example if it was today, then 16 March 2016. This needs to change at the top of the document, in the Citation format and in the footer on the PDF>

B.4 - Specification URIs. This version links. These will be as presented here.

For the Latest version links, they need to be as presented. As I said above, we may need to experiment to find out what is needed to ensure that they work properly.

B.5 - Additional artifacts. This section lists all the other parts that together make up the spec. If you had any other files such as schemas or examples, they would be listed here as well. Note the "(this document)" flag that is added to the title that corresponds to the current document.

B.6 - Related work. You would put the titles and pointers to any content either replaced by or useful to know about here. For example, you had an outside link for the Link Guidance document. You'll find that I put it there. If you don't have anything for the section (probably the case for the rest of the documents) then simply delete that section.

B.7 Abstract - put your abstract here.

B.8 Status - this is OASIS boilerplate and needs to go into your version, links included, exactly as it appears here.

B.9 Citation format - I modified the labels and drafted the citation blocks as they should appear in your files.

B.10 Notices - again, OASIS boilerplate that should appear exactly as shown in your spec.

B.11 Footers - obviously not necessary for the HTML but they should be in place for the PDF as shown here.

C. Changing from a csd to a csprd

As I mentioned, you'll need a copy of the spec labelled as being the public review copy. See oslc-core-spec-v3.0-csprd01-part1-overview.html for an example.

To change your csds into csprds, you need make the following changes:

C.1 - work product stage - change "Committee Specification Draft 01" to

Committee Specification Draft 01 / <soft return>
Public Review Draft 01

C.2 - In all the URLs and filenames, change "csd01" to "csprd01." This includes in the Additional artifacts section, the Citation format, and the footer on the PDF.

C.3 - In the Citation format, change "Committee Specification Draft 01" to "Committee Specification Draft 01 / Public Review Draft 01"

D. Approving and submitting the work to OASIS

When you have the documents ready to approve and hand off for publication, here are the steps to take:

- Take all the files to be released as the drafts and zip them into a ZIP file, one for the csd and one for the csprd. Then load them into the TC's document repository in Kavi. This is so that I can, in the future, connect the dots between exactly what the TC voted to approve and what was published on docs.oasis-open.org.

- Approve a motion or ballot using language such as:

I move that the TC approve <Working draft title, version number and revision number> and all associated artifacts packaged together in <URL to ZIP file in TC's Kavi document repository> as a Committee Specification Draft and designate the <format> version of the specification as authoritative.

and

I move that the TC approve submitting the public review version of <Working draft title, version number> contained in <URL to CSD on docs.oasis-open.org OR ZIP file in TC's Kavi document repository> for 30 day public review.

- After the minutes of that meeting are published or the ballot is closed, you can submit the request to TC Admin using the form at https://www.oasis-open.org/resources/tc-admin-requests/30-day-committee-specification-draft-public-review-request

I will take it from there.

This is a lot of detail to take in, I know. Have a look over it and then let me know if you have any questions or want to go over it by phone.

