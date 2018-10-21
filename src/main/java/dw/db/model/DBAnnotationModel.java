package dw.db.model;

import dw.db.annotation.DwDbFld;
import dw.db.annotation.DwDbTbl;

import java.util.HashMap;
import java.util.Map;

public class DBAnnotationModel
{
	Map<String,DwDbFld>	dwDbFldMaps	= new HashMap<>();
	DwDbTbl				dwDbTbl		= null;

	public Map<String,DwDbFld> getDwDbFlds()
	{
		return dwDbFldMaps;
	}

	public void addDwDbFld(String fldName, DwDbFld dwDbFld)
	{
		dwDbFldMaps.put(fldName, dwDbFld);
	}

	public DwDbTbl getDwDbTbl()
	{
		return dwDbTbl;
	}

	public void setDwDbTbl(DwDbTbl dwDbTbl)
	{
		this.dwDbTbl = dwDbTbl;
	}
}
