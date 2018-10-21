package dw.db.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AutoCodeInfoModel
{
	Map<String,List<String>> dwModelDirDef;
	Map<String,List<String>> dwDaoDirDef;
}
