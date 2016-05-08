package com.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.domain.ColumnInfo;
import com.service.CompareService;
import com.util.DbUtil;
import com.util.HttpUtil;
import com.util.JsonUtil;

/**
 * 前端控制器
 * @author MX
 * @date 2016年3月18日 下午7:18:01
 */
@Controller
@RequestMapping("/")
public class AppController {
	
	private static transient Log log = LogFactory.getLog(AppController.class);
	
	@Autowired
	private CompareService compareService;
	
	/**
	 * 进入首页
	 * @author MX
	 * @date 2016年3月18日 下午7:17:30
	 * @return
	 * @throws Exception
	 */
	@RequestMapping
	public String index(ModelMap model,HttpServletRequest request)throws Exception {
		model.put("basePath", request.getContextPath());
		return "index";
	}
	
	/**
	 * 进入数据库配置界面
	 * @author MX
	 * @date 2016年3月18日 下午7:17:24
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/db")
	public String db(ModelMap model)throws Exception {
		model.put("dbs", DbUtil.query("SELECT * FROM DB"));
		return "db/index";
	}
	
	/**
	 * 编辑数据库
	 * @author MX
	 * @date 2016年3月18日 下午8:37:09
	 * @param model
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/db/edit")
	public String dbEdit(ModelMap model,HttpServletRequest request)throws Exception {
		String id = request.getParameter("ID");
		if(!StringUtils.isEmpty(id)){
			Map<String, Object> data = DbUtil.queryRow("SELECT * FROM DB WHERE ID = ?", id);
			model.put("data", data);
		}else{
			model.put("db_conf", JsonUtil.toJsonStr(DbUtil.DB_PROPS));
		}
		return "db/edit";
	}
	
	/**
	 * 删除数据库配置
	 * @author MX
	 * @date 2016年3月18日 下午9:37:40
	 * @param model
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/db/del/{id}")
	public @ResponseBody Object dbDel(@PathVariable String id)throws Exception {
		Map<String, Object> model = new HashMap<>();
		DbUtil.execute("DELETE FROM DB WHERE ID = ?", id);
		model.put("success", true);
		return model;
	}
	
	/**
	 * 保存数据库配置
	 * @author MX
	 * @date 2016年3月18日 下午9:48:36
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/db/save")
	public @ResponseBody Object dbSave()throws Exception {
		Map<String, Object> model = new HashMap<>();
		Map<String, Object> data = HttpUtil.getParameterMap();
		DbUtil.saveOrUpdate("DB", data);
		model.put("success", true);
		return model;
	}
	
	@RequestMapping("/db/test/{id}")
	public @ResponseBody Object dbTest(@PathVariable String id)throws Exception {
		Map<String, Object> retVal = new HashMap<>();
		try{
			DbUtil.getConn(id);
			retVal.put("success", true);
		}catch(Exception e){
			log.error(this,e);
		}
		return retVal;
	}
	
	/**
	 * 进入版本控制页面
	 * @author MX
	 * @date 2016年3月19日 下午6:24:08
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/version")
	public String version(ModelMap model)throws Exception {
		StringBuilder buf = new StringBuilder();
		buf.append("SELECT A.ID,B.CODE AS DB_CODE,B.NAME AS DB_NAME,A.DESCR,A.CREATE_DATE,")
		   .append("(SELECT COUNT(*) FROM DB_DETAIL WHERE VERSION_ID = A.ID) AS TOTAL_COUNT,")
		   .append("(SELECT COUNT(DISTINCT TABLE_NAME) FROM DB_DETAIL WHERE VERSION_ID = A.ID) AS TABLE_COUNT ")
		   .append("FROM VERSION A LEFT JOIN DB B ON A.DB_ID = B.ID ORDER BY A.CREATE_DATE DESC");
		model.put("versions", DbUtil.query(buf.toString()));
		model.put("dbs", DbUtil.query("SELECT ID,NAME,CODE FROM DB ORDER BY ID"));
		return "version/index";
	}
	
	/**
	 * 添加数据库版本
	 * @author MX
	 * @date 2016年3月19日 下午6:48:09
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/version/add")
	public @ResponseBody Object versionAdd()throws Exception {
		Map<String, Object> model = new HashMap<>();
		String dbid = HttpUtil.getParameter("DB_ID");
		if(StringUtils.isEmpty(dbid)){
			throw new Exception("请选择数据库");
		}
		String descr = HttpUtil.getParameter("DESCR");
		compareService.createVersion(dbid, descr);
		model.put("success", true);
		return model;
	}
	
	/**
	 * 删除某个版本信息
	 * @author MX
	 * @date 2016年3月19日 下午8:28:35
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/version/del/{id}")
	public @ResponseBody Object versionDel(@PathVariable String id)throws Exception {
		compareService.deleteVersion(id);
		return Collections.singletonMap("success", true);
	}
	
	/**
	 * 上传版本文件
	 * @author cxxyjsj
	 * @date 2016年5月8日 上午9:17:08
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/version/upload")
	public @ResponseBody Map<String, Object> upload(@RequestParam("file") MultipartFile file,
			@RequestParam String DB_ID)throws Exception {
		
		return null;
	}
	
	/**
	 * 进入比较页面
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/compare")
	public String compare(ModelMap model)throws Exception {
		model.put("versions", DbUtil.query("SELECT A.ID,B.CODE,B.NAME,A.CREATE_DATE FROM VERSION A "
				+ "LEFT JOIN DB B ON A.DB_ID = B.ID ORDER BY A.CREATE_DATE DESC"));
		return "compare/index";
	}
	
	/**
	 * 开始比较
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/compare/start")
	public String compareStart(ModelMap model)throws Exception {
		String srcId = HttpUtil.getParameter("SRC_ID");
		String tarId = HttpUtil.getParameter("TAR_ID");
		String condition = HttpUtil.getParameter("CONDITION");
		String cond = null;
		if(!StringUtils.isEmpty(condition)){
			cond = condition.replaceAll("@tableName", "TABLE_NAME");
		}
		if(!StringUtils.isEmpty(srcId) && !StringUtils.isEmpty(tarId)){
			model.put("srcId", srcId);
			model.put("tarId", tarId);
			// 获取开发环境比现场环境多的表
			StringBuilder buf = new StringBuilder();
			buf.append("SELECT DISTINCT TABLE_NAME FROM DB_DETAIL WHERE VERSION_ID = ? ");
			if(!StringUtils.isEmpty(cond)){
				buf.append(" AND (").append(cond).append(") ");
			}
			buf.append(" AND TABLE_NAME NOT IN(")
			   .append("SELECT DISTINCT TABLE_NAME FROM DB_DETAIL WHERE VERSION_ID = ? ");
			if(!StringUtils.isEmpty(cond)){
				buf.append(" AND (").append(cond).append(") ");
			}
			buf.append(") ORDER BY TABLE_NAME");
			model.put("moreTables", DbUtil.queryOnes(buf.toString(), srcId,tarId));
			
			// 获取开发环境比现场环境少的表
			model.put("lessTables", DbUtil.queryOnes(buf.toString(), tarId,srcId));
			
			buf.setLength(0);
			buf.append("SELECT DISTINCT TABLE_NAME FROM DB_DETAIL WHERE VERSION_ID = ? ");
			if(!StringUtils.isEmpty(cond)){
				buf.append(" AND (").append(cond).append(") ");
			}
			buf.append(" AND TABLE_NAME IN(")
			   .append("SELECT DISTINCT TABLE_NAME FROM DB_DETAIL WHERE VERSION_ID = ? ");
			if(!StringUtils.isEmpty(cond)){
				buf.append(" AND (").append(cond).append(") ");
			}
			buf.append(")");
			// 获取相同的表名
			List<Object> sameTables = DbUtil.queryOnes(buf.toString(), tarId,srcId);
			// 获取有差异的表
			List<String> diffTables = compareService.getDiffTables(srcId, tarId, sameTables);
			model.put("diffTables", diffTables);
		}
		return "compare/result";
	}
	
	/**
	 * 比较表结构不同
	 * @param model
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/compare/diff/{srcId}_{tarId}/{tableName}")
	public String compareTableDetail(ModelMap model,@PathVariable String tableName,@PathVariable String srcId,
			@PathVariable String tarId)throws Exception {
		String sql = "SELECT TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,COLUMN_SIZE FROM DB_DETAIL "
				+ "WHERE VERSION_ID = ? AND TABLE_NAME = ?";
		
		List<ColumnInfo> srcCols = DbUtil.queryColumns(sql, srcId,tableName);
		List<ColumnInfo> tarCols = DbUtil.queryColumns(sql, tarId,tableName);
		
		List<ColumnInfo> distCols = new ArrayList<>();
		if(srcCols != null && srcCols.size() > 0){
			for(int i=0;i<srcCols.size();i++){
				ColumnInfo ci = srcCols.get(i);
				if(!distCols.contains(ci)){
					ci.setDb("SRC");
					distCols.add(ci);
				}
			}
		}
		if(tarCols != null && tarCols.size() > 0){
			for(int i=0;i<tarCols.size();i++){
				ColumnInfo ci = tarCols.get(i);
				if(distCols.contains(ci)){
					distCols.remove(ci);
				}else{
					ci.setDb("TAR");
					distCols.add(ci);
				}
			}
		}
		Map<String, Map<String,Object>> map = new HashMap<>();
		for(int i=0;i<distCols.size();i++){
			ColumnInfo col = distCols.get(i);
			String name = col.getName();
			Map<String, Object> m = map.get(name);
			if(m == null){
				m = new HashMap<>();
				map.put(name, m);
			}
			String db = col.getDb();
			m.put(db + "_NAME", col.getName());
			m.put(db + "_TYPE", col.getType());
			m.put(db + "_SIZE", col.getSize());
		}
		model.put("cols", map.values());
		return "compare/table_diff";
	}
	
	/**
	 * 导出变更脚本
	 * @author cxxyjsj
	 * @date 2016年5月1日 下午8:15:17
	 * @param tableName
	 * @param srcId
	 * @param tarId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/export/{srcId}_{tarId}")
	public ResponseEntity<byte[]> exportChangeSql(@PathVariable String srcId,
			@PathVariable String tarId)throws Exception {
		StringBuilder buf = new StringBuilder();
		buf.append("SELECT DISTINCT TABLE_NAME FROM DB_DETAIL WHERE VERSION_ID = ? ");
		buf.append(" AND TABLE_NAME IN(")
		   .append("SELECT DISTINCT TABLE_NAME FROM DB_DETAIL WHERE VERSION_ID = ? ");
		buf.append(")");
		// 获取相同的表名
		List<Object> sameTables = DbUtil.queryOnes(buf.toString(), tarId,srcId);
		// 获取有差异的表
		List<String> diffTables = compareService.getDiffTables(srcId, tarId, sameTables);
		if(diffTables == null || diffTables.size() < 1){
			return null;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		// 获取两个版本中所有表结构变动. 只针对目标表进行新增和修改操作
		String sql = "SELECT TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,COLUMN_SIZE FROM DB_DETAIL "
				+ "WHERE VERSION_ID = ? AND TABLE_NAME = ?";
		String type = (String)DbUtil.queryOne("SELECT TYPE FROM DB WHERE ID = (SELECT DB_ID FROM"
				+ " VERSION WHERE ID = ?)", tarId);
		for(String tableName : diffTables){
			List<ColumnInfo> srcCols = DbUtil.queryColumns(sql, srcId,tableName);
			List<ColumnInfo> tarCols = DbUtil.queryColumns(sql, tarId,tableName);
			String result = compareService.getChangeSql(type, tableName, srcCols, tarCols);
			pw.println(result);
		}
		String results = sw.toString();
		pw.close();
		
	 	HttpHeaders headers = new HttpHeaders();    
        String fileName= "Database change.sql";
        headers.setContentDispositionFormData("attachment", fileName);   
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<byte[]>(results.getBytes(Charset.forName("UTF-8")), headers, HttpStatus.CREATED);    
	}
}
