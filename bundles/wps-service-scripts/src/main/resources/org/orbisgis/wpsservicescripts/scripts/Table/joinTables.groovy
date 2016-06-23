package org.orbisgis.wpsservice.scripts

import org.orbisgis.wpsgroovyapi.input.*
import org.orbisgis.wpsgroovyapi.output.*
import org.orbisgis.wpsgroovyapi.process.*

/********************/
/** Process method **/
/********************/



/**
 * This process joins two tables.
 * @return A database table or a file.
 * @author Erwan Bocher
 */
@Process(title = "Table join",
        resume = "Join two tables.",
        keywords = "Table,Join")
def processing() {

	if(createIndex!=null && createIndex==true){
		sql.execute "create index on "+ rightDataStore + "("+ rightField[0] +")"
		sql.execute "create index on "+ leftDataStore + "("+ leftField[0] +")"
	}

	String query = "CREATE TABLE "+outputTableName+" AS SELECT * FROM "

	if(operation.equals("left")){
		query += leftDataStore + "JOIN " + rightDataStore + " ON " + leftDataStore+ "."+ leftField[0]+ "="+ rightDataStore+"."+ rightField[0];
	}
	else if (operation.equals("left")){

	}
	//Execute the query
	sql.execute(query);

	//SELECT *
	//FROM A
	//LEFT JOIN B ON A.key = B.key

	//SELECT *
	//FROM A
	//RIGHT JOIN B ON A.key = B.key

	//INNER JOIN
	//SELECT *
	//FROM A
	//INNER JOIN B ON A.key = B.key
	literalOutput = "Process done"
}


/****************/
/** INPUT Data **/
/****************/

/** This DataStore is the left data source. */
@DataStoreInput(
        title = "Left data source",
        resume = "The left data source used for the join.")
String leftDataStore

/** This DataStore is the right data source. */
@DataStoreInput(
        title = "Right data source",
        resume = "The right data source used for the join.")
String rightDataStore

/**********************/
/** INPUT Parameters **/
/**********************/

/** Name of the identifier field of the left dataStore. */
@DataFieldInput(
        title = "Left field",
        resume = "The field identifier of the left data source",
        dataStoreTitle = "Left data source",
        excludedTypes = ["GEOMETRY"])
String[] leftField

/** Name of the identifier field of the right dataStore. */
@DataFieldInput(
        title = "Right field",
        resume = "The field identifier of the right data source",
        dataStoreTitle = "Right data source",
        excludedTypes = ["GEOMETRY"])
String[] rightField


@EnumerationInput(title="Operation",
        resume="Types of join.",
        values=["left","right", "union"],
        names=["Left join","Right join", "Union join" ],
        selectedValues = "left",
multiSelection = false)
String operation


@LiteralDataInput(
		title="Create indexes",
		resume="Create an index on each field identifiers to perform the join.",
		minOccurs = 0)
Boolean createIndex


@LiteralDataInput(
		title="Output table name",
		resume="Name of the table containing the result of the process.")
String outputTableName

/*****************/
/** OUTPUT Data **/
/*****************/

/** String output of the process. */
@LiteralDataOutput(
		title="Output message",
		resume="The output message")
String literalOutput

