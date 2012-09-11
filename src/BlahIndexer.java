import org.solrmarc.index.SolrIndexer;
import org.marc4j.marc.*;

public class BlahIndexer extends SolrIndexer
{
   //Custom methods for Blacklight@Hull Instance of Solr-marc
   //Simon W Lamb - 13 June 2012
    public BlahIndexer(String propertiesMapFile, String propertyPaths[])
    {
        super(propertiesMapFile, propertyPaths);
    }


  /**
   * Extract the Bibliographic number from the record
   * and remove any occurances of '.' from it, to enable  it 
   * be used as a Solr ID.
   *
   * @param  Record     record
   * @return bibField     Bib number
   */
    public String getBibRecordNo(Record record)
    {
      String bibField = getFirstFieldVal(record, "907a");

      if (bibField != null)
      {
        return bibField.replace(".", "");   
      }
      else
      {
        return bibField;
      }
    }

  /**
   * Return whether the record is unsuppressed or not 
   * if it is suppressed return as null,
   * this method enables us to use the DeleteRecordIfFieldEmpty method
   * in index.properties to delete suppressed fields.` 
   *
   * @param  Record          record
   * @return      
   */
    public String returnSuppressedRecordAsNull(Record record)
    {
      String suppressField = getFirstFieldVal(record, "998f");

      //if the field doesn't exist we will return null..
      if (suppressField == null) {
        return null;
      }
      else {
        if (suppressField.trim().equals("-")) {
          return "unsuppressed";
        }
        else
        {
          return null;
        }
      }
    }
//  other custom indexing functions go here


  /**
   * Returns a Cast list if it exists for a record 
   * Cast list is stored in 511a Indicator field One = 1   
   *
   * @param  LinkedHashSet          resultSet
   * @return      
   */
  public String getRecordCastList(Record record)
  {
    Set resultSet = new LinkedHashSet();
    List fields = getVariableFields(record, "511");

    for (Object field : fields)
    {
      if (field instanceof DataField)
      {
        DataField dField = (DataField)field;
        
        if (dField.getIndicator1() == 1) 
        {
          if (dField.getSubfield('a') != null) 
          {
            resultSet.add(dField.getSubfield('a').getData());
          }          
        }  
      } 
    }
    return resultSet;
  }

 /**
   * Returns a Performers list if it exists for a record 
   * Cast list is stored in 511a Indicator field One != 1   
   *
   * @param  LinkedHashSet          resultSet
   * @return      
   */
  public String getRecordPerformerList(Record record)
  {
    Set resultSet = new LinkedHashSet();
    List fields = getVariableFields(record, "511");

    for (Object field : fields)
    {
      if (field instanceof DataField)
      {
        DataField dField = (DataField)field;
        
        if (dField.getIndicator1() != 1)
        {
          if (dField.getSubfield('a') != null) 
          {
            resultSet.add(dField.getSubfield('a').getData());
          }          
        }  
      } 
    }
    return resultSet;
  }

  /**
   * Enables you to getFields by the first and second indicators, fieldNo and subFieldString (use null for no subfield )
   * fieldNumbers can multiple fields by sperating the fields using the ":" char.  
   *
   * @param  LinkedHashSet          resultSet
   * @return      
   */
   public String getFieldsByIndicators(Record record, String fieldNumbers, String subFieldString, String firstIndicator, String secondIndicator)
   {
    Set resultSet = new LinkedHashSet();  
    String[] fieldArray;

    //If the fieldNo variable contains more than one fieldNo (split by the ":" char) then add them fieldArray
    //Otherwise just add the single field to the same array
    if fieldNumbers.contains(":") {
      fieldArray = fieldNumbers.split("\\:")
    }
    else {
      fieldArray = new String[] { fieldNumbers }
    }

    //Loop around all the fieldNo
    for (String fieldNo in fieldArray)
    { 
      List fields = getVariableFields(record, fieldNo);

      //Loop around all the fields within the fieldNo
      for (Object field : fields)
      {
        if (field instanceof DataField)
        {
          DataField dField = (DataField)field;
          
          //Compare the dataField with the indicators specified in the method params
          if (dField.getIndicator1() == firstIndicator && dField.getIndicator2() == secondIndicator)
          {
            //If null has been entered as a subField we get all the subField entries
            if (subFieldString == "null") {
              List subFieldList = dField.getSubfieldList(); 
    
              for (Object subField : subFieldList)
              {
                if (subField instanceof Subfield)
                {
                  resultSet.add(subField.getData());
                }
              }
            }
            //Else we just get the subField entry specified 
            else 
            {
              char subFieldChar = subFieldString.charAt(0);
              if (dField.getSubfield(subFieldChar) != null) 
              {
                resultSet.add(dField.getSubfield(subFieldChar).getData());
              }     
            }
          }  
        } 
      }
    }
    return resultSet;
  }


}
