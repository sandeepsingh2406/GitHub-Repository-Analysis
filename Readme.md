**This repo contains the Final project i.e. **GitHub Repository Analysis**

Project Members are: Abhijay Patne , Sandeep Singh**

---------------------------------------------------------------------
**Highlights and features of the application:
**
Use of multiple Google Cloud tools

Recommender system for Repositories

Web Service hosted in cloud(give IP address)

Implemented in Scala for extra bonus :)

--------------------------------------------------------------------

**Project Flow
**

As a first step, In **GithubProcessor**.scala we create actors to download github repository details in the form of JSONs using some API search queries. The username of the owners of these github repositories  are also extracted, and JSONs containing their information are also obtained using the GitHub API. These different JSON strings are written to MongoDB(hosted in Google Cloud) using API calls. All the different tasks mentioned here are executed using different actors which communicate with each other using messages. **MongoDBOperationAPIs**.scala contains different methods which make these API calls to mongoDB, and these methods have been utilized by the actors mentioned previously.


After MongoDB has been filled with information about github repositories and users, in **MongoDbToMySql**.scala we create an actor to clone github repositories(whose names and other details are stored in mongoDB) to extract some extra metadata about them. We clone these repositories using the JGIT library. Another actor is created to insert some filtered metadata about users and repositories into MySQL, which is also hosted in Google Cloud. **MySQLOperationAPIs**.scala contains methods that interact with the MySQL through API calls,  and these method are utilized by different actors to interact with MySQL.


Lastly, once the MySQL storage has been set up, we can run **WebService**.scala which creates and starts a web service using Akka HTTP. This web service takes different parameters from the user, which can be used for getting some analytical results. The web service intercepts user’s requests and interacts with **MySQLOperationAPIs**.scala to retrieve information from MySQL. This information contains details of the kinds of  repositories or users which the user is interested in. The web service provides a response to the user in the form of JSONs.


**SLF4J** has been used to log information from the different class mentioned above, and the log created can be utilized to analyze the entire application’s execution.


**RapidMiner
**

We have used RapidMiner to find the patterns from downloaded data. RapidMiner can import data from flat files or database. We have imported data from our MySQL data store and stored in RapidMiner’s Local Repository [Fig 1]. RapidMiner has reach set of APIs to process this data, build Machine Learning models on them. We created processes for different analysis steps we performed. Snippet of one of them is mentioned below [Fig 2].

---------------------------------------------------------------------------------

**Project Structure
**

*Scala main classes(/src/main/scala/):
*


**GithubProcessor**.scala: This is the first class file that should be run when trying to setup the entire project and databases. (If databases have already been loaded, WebService.scala can be run directly)


An object named githubProcessor has been created which instantiates our main class, named “initializerClass”. We create 3 actors here: downloaderActor, jsonParser and mongoDbConnector.
Firstly, this classes passed a message to the actor downloaderActor which downloads repositories metadata and sends it for further processing.


**Actors**
downloaderActor This actor downloads the repo details for github repositories specified by language, created date, and lower limit for size. All github queries that we have used have been mentioned later.

The repo details are retrieved in the form of JSON strings which are stored in files locally. Instead of downloading these locally, we can also process them on the fly. Since github has a restriction on the number of API calls we can make in one minute(for some API calls these limits are also per hour), our actor goes to sleep until the next minute start so that we do not encounter a “limit exceeded” response from the GitHub API call.

Next, a message is passed to the second actor “jsonParser“ which parses the downloaded JSON strings.
After processing of the JSON strings this actor jsonParser gives a message back to downloadActor, which downloads the user details through GitHub API calls. These user names are extracted by the jsonParser actor described below.


**jsonParser**: This actor reads the JSON string which had been downloaded and saved locally as mentioned before. It processes the json to extract the username for the owner of the repository. Later, it send various messages to the third actor, “mongoDbConnector” providing it the entire repo JSONs.


**mongoDbConnector**: This is the third and the final actor in this file. This actor gets the user/repo JSON from the second actor, and uses an API call to MongoDB to write the JSON there, in the appropriate collection. This API call is made by calling a method in MongoDBOperationAPIs.scala (described later).


**HttpBasicAuth**: This class is used by other actors to make the GitHub API calls with authentication. GitHub allows a higher limit for authenticated API calls, so this comes in very handy whenever we make multiple API calls to GitHub.


2.	**MongoDbToMySql**.scala: This class uses the Akka actor system to read data from MongoDB, download some extra metadata, and write the filtered information to MySQL. This class should be executed after MongoDB has been populated by GithubProcessor.scala.


Different behaviours in the actor “mongoDbReaderActor” are sent messages, which initiate the process of extracting information from MongoDB.


**mongoDbReaderActor**: After getting JSONs for different repos from MongoDB, we extract their URLs and passes these as messages to the actor getMetadataJgit.


This actor also gets the entire user JSONs from MongoDB, extracts the user subscription url(is used to get some extra information about the user) and makes API calls to Github to get this user metadata. Again, authentication is used for Github API calls to get a higher limit. This user metadata and repo metadata is passed onto another actor.


**getMetadataJgit**: This actor clones the repositories using Jgit library to obtain some extra relevant information like total lines of code, total number of files, different languages used, number of commits etc.
Subsequently, all the filtered details of users and repos and passed onto the actor mySqlWriterActor.


**mySqlWriterActor**: This actor performs one basic function: write all the data it receives to MySQL into specified tables.
Different cases have been created in the actor to receive different kinds of data, and write them to different tables in MySQL.
There are separate methods defined in MySQLOperationAPIs.scala which make API calls to MySQL to insert rows into different tables. These methods are used by this actor to write into the database.


3. **MongoDBOperationAPIs**.scala(EXPAND, ABHIJAY)


4. **MySQLOperationAPIs**.scala(EXPAND, ABHIJAY)


**ParameterConstants**.scala (Abhijay)


5. **WebService**.scala


The web service(created using Akka HTTP) listens for REST calls to produce different types of analytical results based on the information in the MySQL database.



Once a request is received, the request parameters are obtained, and accordingly a SQL query made to the MySQL database, which provides a response which can be given as output to the rest call. The output of the web service is formatted in the form of a Json response.
Various web service parameters are  described in the How To Run section and their analytics results are described in the Results Analysis section.

------------------------------------------------------------------------------

**How to run the project:
**

**OPTION 1(Run everything locally):
**Clone the repo and import the project into IntelliJ using SBT.

All classes can be run individually, GithubProcessor.scala and WebService.scala, as they are independent.

If starting from scratch, GithubProcessor.scala is the first class file that should be run when trying to setup the entire project and databases. MongoDbToMySql.scala should be executed after MongoDB has been populated by GithubProcessor.scala.

Now run WebService.scala -> Right click and run WebService to run it locally and then use the web service url in your browser (http://localhost:8080/). The web service can be run directly if MySQL has already been setup.

Note: While running the scala programs for the first time, IntelliJ might show the error, "Module not defined". You can go to Run->Edit Configurations->Use classpath of module and specify the module there. And then rerun the program.


**OPTION 2(Run web service and other programs in the cloud):
**

1.      Copy build.sbt, and /src/main/ to a folder in your google cloud VM. Run using SBT(From within the folder):

2.      Run the commands:        
               
               sbt compile
   
               sbt run

**Note**: In our build.sbt we have specified the mainClass as WebService.scala. To run other classes specify them here in build.sbt and replace WebService.scala

After the web service is created, the URL to access it is http://localhost:8080 (if web service is run locally OR use your google cloud external IP)
Different rest calls that can be made to the web service. Instructions to use the web service created are given below.(These instructions can also be found when you browse to http://localhost:8080 using a browser)
 Note: If simply clicking the URL doesn't work, copy it and paste in your browser.

**Examples**:

Below queries fetch the top repository owners for GitHub in our database, according to different sorting criteria. (Number of users to be displayed can be specified)



                 http://localhost:8080/?topUsers=5&sortBy=followersCount

                 http://localhost:8080/?topUsers=5&sortBy=followingCount

                 http://localhost:8080/?topUsers=5&sortBy=publicReposCount

                 http://localhost:8080/?topUsers=5&sortBy=subscriptionsCount

To get the top repositories according to popularity (defined as a function of watchers and forks counts) (number of users to be displayed can be specified):


                 http://localhost:8080/?topRepo=10

To get average lines of code per language (number of languages to be displayed can be specified):


                 http://localhost:8080/avgLocPerLanguage

To get top languages, selected by number of repositories in each language (number of languages to be displayed can be specified):


                 http://localhost:8080/?topLanguages=5

**Examples for Repository Recommendation:
**

The user enters a repository name, and we try to recommend some similar repositories.


                 http://localhost:8080/?getRecommendation=sprintnba

                 http://localhost:8080/?getRecommendation=deep_recommend_system

                 http://localhost:8080/?getRecommendation=emoji-mart

------------------------------------------------------------------------------------

References: present in "documents/references.txt"