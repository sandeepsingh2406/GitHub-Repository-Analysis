/**
  * Created by avp on 12/1/2016.
  *
  * ToDo
  * close connection client
  */

import java.sql.{Connection,DriverManager}

object MySQLOperationAPIs {

//  val url = "jdbc:mysql://localhost:8889/mysql";
  val url = ParameterConstants.mysqlPrefix + "://" + ParameterConstants.mysqlHostIPAddress +
  ":" + ParameterConstants.mysqlDBPortNumber + "/" + ParameterConstants.mysqlDBName;
  var connection:Connection = DriverManager.getConnection(url, ParameterConstants.mysqlUserName, ParameterConstants.mysqlPassword);

  def main(args: Array[String]): Unit ={
    println("hello");
  }
}
