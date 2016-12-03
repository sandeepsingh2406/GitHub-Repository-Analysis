/**
  * Created by avp on 12/1/2016.
  *
  * ToDo
  * close connection client
  */

import java.beans.Statement
import java.sql.{Connection, DriverManager}

object MySQLOperationAPIs {

//  val url = "jdbc:mysql://localhost:8889/mysql";
  val url = ParameterConstants.mysqlPrefix + "://" + ParameterConstants.mysqlHostIPAddress +
  ":" + ParameterConstants.mysqlDBPortNumber + "/" + ParameterConstants.mysqlDBName;
  //      ":" + ParameterConstants.mysqlDBPortNumber + "/" + ParameterConstants.mysqlDBName;
  //      "/" + ParameterConstants.mysqlDBName;

  Class.forName(ParameterConstants.mysqlDriver);
  println ("reached here");
  var connection:Connection = DriverManager.getConnection(url, ParameterConstants.mysqlUserName, ParameterConstants.mysqlPassword);
  println(insertTopRepoCommitsTable("sdf", 123, 123, 245));
  def main(args: Array[String]): Unit ={
  }


  // insert query for topRepoCommitsTable
  def insertTopRepoCommitsTable(repoName:String, repoID:Long, numberOfCommits:Int, numberOfFiles:Int): Int = {
    val query = "INSERT INTO `toprepocommitstable`(`repoName`, `repoID`, `numberOfCommits`, `numberOfFiles`) VALUES (\"" +
      repoName + "\", " + repoID + ", " + numberOfCommits + ", " + numberOfFiles + ");";
    var result = -1;
    try {
      val statement = connection.createStatement();
      result = statement.executeUpdate(query);
    } catch {
      case e => println("Exception in insertTopRepoCommitsTable()");
    }
    return result;
  }

  def testDBConnection(): Unit = {
    val statement = connection.createStatement;
    val resultSet = statement.executeQuery("show tables;");
    while (resultSet.next) {
      println(resultSet.getString("Tables_in_cs441project"));
    }
  }
}
