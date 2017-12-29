I have implemented six vulnerabilities from the OWASP Top 10 vulnerability list. The vulnerabilities I implemented were, Broken Authentication and Session Management, Cross-Site Scripting, Insecure Direct Object References, Missing Function Level Access Control, Cross-Site Request Forgery and Unvalidated Redirects and Forwards.

To make the vulnerabilities more meaningful in real life I have added some more features to the basic application. The signup functionality is still there, but in addition there are administrative features available if you click the “Admin features” link. These features should require logging in with either username “ted” or username “admin”. Password for both logins is “ted”. The intent is that ted is able to view all the enrollments of the event, but only admin is able to delete the enrollments.

Next, I will walk through all the known vulnerabilities:

# Vulnerability 1. Broken Authentication and Session Management: #
I actually am not sure if my implementation really is vulnerable, but at least this is a feature that should be addressed. The application does not have any logout feature. This means that if one logs in as “ted” or “admin” and closes the browser tab, the session is still available for next user if he visits the same site. This should be fixed by adding a logout link and functionality that expires the session and requires a new login. The application can be found from: https://github.com/tmannermaa/cybersecuritybase-project

## Identification: ##
This is quite easy to identify when testing the application. If you close the tab in browser and open the application again, it does not ask for new authentication. In addition there is no logout button anywhere.

# Vulnerability 2. Cross-Site Scripting, steps to try out: #
1. Go to “localhost:8080/form” and add anything to name and then “<script>alert(111);</script>” to addressed
2. Go to “localhost:8080/form”  and click Admin features, login and you should see the alert popup.
## How to fix: ##
This can be easily fixed by changing thymeleaf templates “signuplist.html” and “signuplistAdmin.html”. Change all instances of “th:utext” to “th:text” and thymeleaf will escape the address text so that it is not executed in the browser.

## Identification: ##
This is possible to be identified with OWASP ZAP tool. First do the Attack scan that inserts many signups to the database, then browse the application with ZAP’s browser and provide the credentials to the browser when you visit the “Admin features”. After this, do a new scan, and ZAP will warn about the scripts in the “localhost:8080/manage” view. The alert name is “Cross Site Scripting (Persistent)”.

# Vulnerability 3. Insecure Direct Object Reference, steps to try out: #
1. Go to “localhost:8080/form” and add one or more signups with random names/addresses.
2. Go to “localhost:8080/form” and click “Admin features”, login with “ted” (pass: “ted”) and you see the list of all enrollments made to the event. (If you have already logged, you can request new login from “localhost:8080/login”
3. Seems that you are not able to delete any signups, but go to “localhost:8080:/delete/1” and you are able to see the confirmation page. If you click delete, you are also able to delete the first signup without admin rights.
## Fix: ##
Add logic to /delete and /remove endpoints to check that the requesting user is the correct user, and allow deleting only on that case.

## Identification: ##
This is not detectable by OWASP ZAP (At least I did not get it to warn about this). I think these kind of vulnerabilities are usually identified by a curious human attacker.

# Vulnerability 4. Missing Function Level Access Control, steps to try out: #
There is one endpoint /removeAll that is completely missing the authentication. 
1. Go to “localhost:8080/form” and add some signups with random names/addresses
2. Use curl to delete all the signups with following command “curl -d -X POST http://localhost:8080/removeAll/”
3. Go to “localhost:8080/manage” and see that all signups have disappeared
## Fix: ##
The SecurityConfiguration.java now explicitely lists the endpoints that require authentication and I have forgotten to add /removeAll to the list. Better way would be to list only the endpoints that do not require authentication, that is to change the configuration as follows:         http.authorizeRequests()
                .antMatchers("/form").permitAll()
                .antMatchers("/").permitAll()
                .anyRequest().authenticated();
                http.formLogin().permitAll();

## Identification: ##
This is not detectable by OWASP ZAP (At least I did not get it to warn about this). I think these kind of vulnerabilities are usually identified by a curious human attacker.

# Vulnerability 5. Cross-Site Request Forgery, steps to try out: #
1. Go to “localhost:8080/form” and add some signups with random names/addresses
2. Go to “localhost:8080/manage” and authenticate with username “admin” and password “ted”.
3. Open CSRF.html with browser. It contains following html: 
<form action="http://localhost:8080/removeAll" method="post">
<input type="submit"
	value="Win Money!"/>
</form>
4. Click button “Win Money” and you will be forwarded to “localhost:8080/manage”. All the signups are gone :(.

## Fix: ## 
This can be very easily fixed by removing the line that disables csrf protection. Delete the line “http.csrf().disable()” and spring will automatically add csrf token to all forms, which makes this attack impossible.

## Identification: ##
This is not detectable by OWASP ZAP (At least I did not get it to warn about this). I think these kind of vulnerabilities are usually identified by a curious human attacker.

## Vulnerability 6: Unvalidated Redirects and Forwards, steps to try out: ##
1. Go to “localhost:8080/form” and hover mouse over the link “Admin features”. There is a weird redirect link.
2. Copy the link and change it slightly, to for example:  http://localhost:8080/redirect?url=www.google.com
3. Enter the modified link to browser and you are redirected to google.com.

## How to fix: ##
This is completely unnecessary feature for the this application. To remove this I would modify the form thymeleaf template to use a link instead: a href="/manage">Admin features</a>. Second remote the /redirect endpoint that is not needed anymore.

## Identification: ##
This vulnerability is actually automatically detected by Owasp ZAP Attack scan. If you scan http://localhost:8080 with OWASP ZAP, the tool will report this as and External Redirect alert. 
