package dw.code.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AutoCodeInfoModel
{
	private Map<String,List<String>> dwModelDirDef; //Model包路径配置
	private Map<String,List<String>> dwDaoDirDef;//Dao包路径配置
}
