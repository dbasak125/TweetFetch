/** Sample Twitter data-scraper
 *  Search API Documentation: from https://dev.twitter.com/rest/public/search
 *  
 *  @author dbasak
 *  
 */

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;
import twitter4j.conf.*;
import twitter4j.*;

import java.util.List;
import java.io.FileOutputStream;
import java.io.*;
import java.util.Date;


public class TweetFetch {
	
	private static int IsRetweet (Status post) throws NullPointerException {
		try {
			long id;
			id = post.getRetweetedStatus().getId();
			return -1;
		} catch (NullPointerException ex) {
			return 0;
		}
		
		//return 0;
	}
	
	private static boolean CheckLimitsOk (int cnt, long strttime) throws InterruptedException {
		try {
			System.out.println("Checking Limits ...");
			int diffsec = (int) ((System.currentTimeMillis() - strttime) / 1000);
			if (cnt >= 180 && diffsec < 900) {
				System.out.println("Sleeping for " + (905 - diffsec) + "seconds ..." + new Date().toString());
				Thread.sleep((905 - diffsec)*1000);
				return false;
			}
			if (diffsec >= 900) {
				return false;
			}
			return true;
		} catch (InterruptedException ex) {
			System.out.println("Exception in CheckLimitsOk method..!!"+ "    " + new Date().toString());
			ex.printStackTrace();
			System.exit(-1);
			return false;
		}
	}
    
	public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("WARNING: args.length is < 1");
            System.exit(-1);
        }
        try(PrintStream out = new PrintStream(new FileOutputStream("./json.json", true))){
            ConfigurationBuilder cb = new ConfigurationBuilder();

            //provide consumer key/secret access key/secret
            cb.setDebugEnabled(true);
            cb.setOAuthConsumerKey("9999999999999999"); //test key
            cb.setOAuthConsumerSecret("9999999999999999");
            cb.setOAuthAccessToken("9999999999999999-9999999999999999");
            cb.setOAuthAccessTokenSecret("9999999999999999");

            Twitter twitter = new TwitterFactory(cb.build()).getInstance();
            try {

                User user = twitter.verifyCredentials();
                System.out.println("Successfully verified user - [" + user.getScreenName() + "]");
                
            } catch (IllegalStateException ie) {
                // access token is already available, or consumer key/secret is not set.
                if (!twitter.getAuthorization().isEnabled()) {
                    System.out.println("OAuth consumer key/secret is not set."+ "    " + new Date().toString());
                    System.exit(-1);
                }
            }
            
            String[] sarr = new String[]{
            		"q=lang:ko since:2016-09-08 until:2016-09-11&count=100"
            		,"q=테니스 lang:ko since:2016-09-18 until:2016-09-19&count=100"
            		,"q=아이폰 lang:ko since:2016-09-05 until:2016-09-18&count=100"
            		,"q=매트 블랙 lang:ko since:2016-09-05 until:2016-09-19&count=100"
            		};
            
            Query query = new Query();
            Query query2 = new Query();
            QueryResult qres;
            int i,j,k;
            long least_id = 0, prev_least_id;
            String json = ""; 

            //counters
            i = 0;
            j = 0;
            k = 0;
            
            long StartTime = System.currentTimeMillis();
            
            for (String s : sarr) {
            	query.setQuery(s);
            	least_id = 0;
            	i=0;
            	
            	System.out.println("<query> "+s);
            	
	            do {
	            	prev_least_id = least_id;
	            	do {
	            		if (!CheckLimitsOk(j, StartTime)) { //wait for 15 mins - standard REST API query limit of 180 per 15 mins apply
	            			System.out.println("Resetting query count (j) and StartTime...");
	            			j = 0;
	            			StartTime = System.currentTimeMillis();
	            		}
		            	j++;
		            	qres = twitter.search(query);
		    			List<Status> posts = qres.getTweets();
		    			
		    			for (Status post : posts) {
		    				if (IsRetweet(post) == 0) {
		    					json = json + "," + TwitterObjectFactory.getRawJSON(post) + "\n";
		    					i++; k++;
		    					System.out.println("query=" +j+ "\t\t tweet number=" +k+ "/" +i+ "\t\t--> " + post.getCreatedAt().toString() + ": " + post.getId() + ": " + post.getLang());
		    				}
		        			
		    				least_id = post.getId() - 1;
		    			}
		    			out.print(json);
		                json = "";
		            } while((query = qres.nextQuery()) != null);
	            	
	            	System.out.println("Prevs_id:====  " + prev_least_id);
	            	System.out.println("Least_id:====  " + least_id);
		            
		            query2.setQuery(s + " max_id:" + least_id); //build the next query from query list - sarr
		            query = query2;
	            } while (!(prev_least_id == least_id) && i<2000);
	            
	            System.out.println("</query>");
	            
            }
            
            //System.out.println("least Post.id:" + least_id);
            System.out.println("\n\nnumber of tweets logged in this batch successfully: " + i + "    " + new Date().toString());
            //System.out.println("re-query: " + j);
            
            
            System.exit(0);
            
        } catch (TwitterException te) {
            System.out.println("Error...!"+ "    " + new Date().toString());
            te.printStackTrace();
            System.exit(-1);
        } catch (FileNotFoundException fnf) {
        	System.out.println("Error...!"+ "    " + new Date().toString());
            fnf.printStackTrace();
            System.exit(-1);
        } catch (InterruptedException intrpt) {
        	System.out.println("Interrupt Error...!"+ "    " + new Date().toString());
        	intrpt.printStackTrace();
            System.exit(-1);
        }
    }
}
