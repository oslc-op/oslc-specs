       /**
       * Queries the back link index for links pointing to the specified target
       * URLs scoped to the specified global configuration URL and
       * specified link types. This is a long running operation.
       *
        * @param targetURLs
       *            must specify at least one, but no more than
       *            {@link ILinkIndexService#MAX_TARGET_URLS}.
       * @param gcURL
       *            required
       * @param linkTypes
       *            must specify at least one. Valid link type constants are
       *            defined in {@link ILinkIndexService}.
       * @return
       * @throws TeamRepositoryException
       */
       public LinkQueryResults queryForLinks(final String[] targetURLs, final String gcURL, final String[] linkTypes) throws TeamRepositoryException;
 
Supporting classes:
 
       public static class LinkQueryResults {
              // zero based page index
              public LinkTriple[] links = new LinkTriple[0];
       }
      
       public static class LinkTriple {
              public String sourceURL;
              public String linkType;
              public String targetURL;
             
              public LinkTriple() {}
             
              public LinkTriple(String sourceURL, String linkType, String targetURL) {
                     this.sourceURL = sourceURL;
                     this.linkType = linkType;
                     this.targetURL = targetURL;
              }
       }
