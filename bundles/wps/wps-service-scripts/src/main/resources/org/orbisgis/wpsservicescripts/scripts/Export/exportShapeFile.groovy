package org.orbisgis.wpsservicescripts.scripts.IO

import org.orbisgis.wpsgroovyapi.input.EnumerationInput
import org.orbisgis.wpsgroovyapi.input.JDBCTableInput
import org.orbisgis.wpsgroovyapi.input.LiteralDataInput
import org.orbisgis.wpsgroovyapi.input.RawDataInput
import org.orbisgis.wpsgroovyapi.output.LiteralDataOutput
import org.orbisgis.wpsgroovyapi.process.Process
import org.h2gis.functions.io.shp.SHPDriverFunction
import org.orbisgis.corejdbc.H2GISProgressMonitor
import org.h2gis.api.DriverFunction


/**
 * @author Erwan Bocher
 */
@Process(title = ["Export into a SHP file","en","Exporter dans un fichier SHP","fr"],
    description = ["Export a table to a SHP file.","en",
                "Exporter une table dans un fichier SHP.","fr"],
    keywords = ["OrbisGIS,Exporter, Fichier, SHP","fr",
                "OrbisGIS,Export, File, SHP","en"],
    properties = ["DBMS_TYPE", "H2GIS","DBMS_TYPE", "POSTGIS"])
def processing() {
    File outputFile = new File(fileDataInput[0])    
    DriverFunction exp = new SHPDriverFunction();
    exp.exportTable(sql.getDataSource().getConnection(), inputJDBCTable, outputFile,new H2GISProgressMonitor(progressMonitor)); 
    if(dropTable){
	sql.execute "drop table if exists " + inputJDBCTable
    }
    literalDataOutput = "The ShapeFile has been created."
}



@JDBCTableInput(
    title = [
                "Table to export","en",
                "Table à exporter","fr"],
    description = [
                "The table that will be exported in a shapeFile file","en",
                "La table à exporter dans un fichier shapeFile.","fr"],
    dataTypes = ["GEOMETRY"])
String inputJDBCTable

@LiteralDataInput(
    title = [
				"Drop the table","en",
				"Supprimer la table","fr"],
    description = [
				"Drop the table when the export is finished.","en",
				"Supprimer la table à l'issue l'export.","fr"])
Boolean dropTable 


/************/
/** OUTPUT **/
/************/

@RawDataInput(
    title = ["Output shapeFile","en","Fichier shapeFile","fr"],
    description = ["The output shapeFile file to be exported.","en",
                "Nom du fichier shapeFile à exporter.","fr"],
    fileTypes = ["shp"], multiSelection=false)
String[] fileDataInput


@LiteralDataOutput(
    title = ["Output message","en",
                "Message de sortie","fr"],
    description = ["Output message.","en",
                "Message de sortie.","fr"])
String literalDataOutput
