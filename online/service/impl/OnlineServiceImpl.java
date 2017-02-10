package com.ctid.business.online.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.ctid.business.online.service.OnlineService;
import com.ctid.business.quotas.service.quotasService;
import com.ctid.core.exception.ServiceException;
import com.ctid.core.hibernate.IHibernateDaoSys;
import com.ctid.util.DateHandler;
import com.ctid.vo.Quotas;

/**
 * 在网用户产品总表实现类
 * 
 * @author 邹永禄
 * @date 2016-08-03
 *
 */

@Service(value = "OnlineServiceImpl")
public class OnlineServiceImpl implements OnlineService {

	Log logger = LogFactory.getLog(OnlineServiceImpl.class);

	@Resource
	private IHibernateDaoSys dao;
	@Resource
	private quotasService quotasServiceImpl;
	@SuppressWarnings("unused")
	private int flag = 0;

	@SuppressWarnings("rawtypes")
	@Override
	public String getOnlineSingleDay(Date date, List<Integer> indexs, int period) {

		Date start = date;

		Date end = DateHandler.GetAfterDay(start, 1);

		Date now = new Date();

		if (now.before(end)) {

			end = now;

		} else {

			end = DateHandler.GetAfterSecond(end, -1);
		}
		if (indexs.contains(10) && indexs.size() > 1) {
			indexs.remove(indexs.size() - 1);
		}

		List lists = this.getData(start, indexs);
		List countList = new ArrayList();
		countList = this.getCount(start, indexs);
		String str = this.buildSingleDayJSON(lists, countList, indexs, start);

		return str;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String getOnlineMutiDay(Date start, Date end, List<Integer> indexs,
			int period) {

		end = DateHandler.GetAfterDay(end, 1);
		Date now = new Date();
		if (now.before(end)) {
			end = now;
		} else {
			end = DateHandler.GetAfterSecond(end, -1);
		}
		if (indexs.contains(10) && indexs.size() > 1) {
			indexs.remove(indexs.size() - 1);
		}
		List<List> lists = new ArrayList<List>();
		List countList = new ArrayList();
		List instlist = null;
		Date st = start;
		int countNum = 0;
		while (st.before(end)) {
			List list = this.getData(st, indexs);
			if (list.size() > 0) {
				instlist = this.inList(list);
			}
			lists.add(instlist);

			List count = this.getCount(st, indexs);
			for (int i = 0; i < ((Object[]) (count.get(0))).length; i++) {
				countList.add(((Object[]) (count.get(0)))[i]);
			}
			countNum++;
			st = DateHandler.GetAfterDay(st, 1);
		}
		String resultHtml = this.buildMutiDayJSON(lists, countList, start,
				period, indexs, countNum);
		return resultHtml;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String getOnlineMonth(int startYear, int startMonth, int endYear,
			int endMonth, List<Integer> indexs, int period) {

		Date start = DateHandler.GetDate(startYear, startMonth, 1);
		Date end = DateHandler.GetDate(endYear, endMonth, 1);

		end = DateHandler.GetAfterMonth(end, 1);

		Date now = new Date();

		if (now.before(end)) {

			end = now;

		} else {

			end = DateHandler.GetAfterSecond(end, -1);
		}
		List<List> lists = new ArrayList<List>();
		List countList = new ArrayList();
		List instlist = null;
		Date st = start;
		int countNum = 0;
		Date ed = DateHandler.GetAfterMonth(st, 1);
		while (st.before(end)) {
			List list = this.getMonthData(st, ed, indexs, period);
			if (list.size() > 0) {
				instlist = this.inList(list);
			}
			lists.add(instlist);

			List count = this.getMonthCount(st, ed, indexs);
			for (int i = 0; i < ((Object[]) (count.get(0))).length; i++) {
				countList.add(((Object[]) (count.get(0)))[i]);
			}
			st = DateHandler.GetAfterMonth(st, 1);
			ed = DateHandler.GetAfterMonth(ed, 1);
			countNum++;
		}
		String resultHtml = this.buildMutiDayJSON(lists, countList, start,
				period, indexs, countNum);
		return resultHtml;

	}

	// 处理查询结果，判断是否新增产品，新增的补0
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<List> inList(List lists) {
		List proList = this.getLie();
		// 重组数据list
		List<List> installList = new ArrayList<List>();
		// List makeList=new ArrayList();
		// 产品相同的计数器
		int count = 0;
		if (lists.size() > 0) {
			// 如果新增产品，新增的补0
			if (proList.size() > lists.size()) {
				// 判断是那个产品是新增产品
				for (int i = 0; i < proList.size(); i++) {
					List makeList = new ArrayList();
					// 产品相同，插原数据 i-count代表当前列
					Object[] sal = ((Object[]) (lists.get(i - count)));
					if (proList.get(i).equals(sal[1])) {
						for (int j = 2; j < sal.length; j++) {
							makeList.add(sal[j]);
						}
					} else {
						// 不相同，则补0处理
						for (int j = 2; j < sal.length; j++) {
							makeList.add(0);
						}
						// 产品不相同计数
						count++;
					}
					installList.add(makeList);
				}
			} else {
				for (int i = 0; i < proList.size(); i++) {
					List makeList = new ArrayList();
					// 产品相同，插原数据 i-count代表当前列
					Object[] sal = ((Object[]) (lists.get(i)));
					for (int j = 2; j < sal.length; j++) {
						makeList.add(sal[j]);
					}
					installList.add(makeList);
				}
			}
		}

		return installList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String buildSingleDayJSON(List lists, List countList,
			List<Integer> indexs, Date start) {

		List<Quotas> quotas = this.quotasServiceImpl.getQuotasIndexs(indexs);

		// 拼接返回json数据字符串
		// mapQuotas代表quotas指标
		List<Map> mapQuotas = new ArrayList<Map>();
		// mapProduct代表产品json
		List<Map> mapProduct = new ArrayList<Map>();
		// mapData代表data
		List<Map> mapData = new ArrayList<Map>();
		// 最后的list
		List<Map> mapsum = new ArrayList<Map>();
		if (lists.size() > 0) {
			Map map = new LinkedHashMap();
			map.put("success", 1);
			map.put("total", lists.size());
			map.put("message", "执行成功");
			Map mapda = new HashMap();
			// 拼接指标
			for (int i = 0; i < quotas.size(); i++) {
				Map mapquota = new HashMap();
				mapquota.put("quotasNameId", quotas.get(i)
						.getQuotaReportNameId());
				mapquota.put("quotasName", quotas.get(i).getQuotaName());
				mapQuotas.add(mapquota);

			}
			mapda.put("quotas", mapQuotas);

			// 拼接产品json
			for (int i = 0; i < lists.size(); i++) {
				Map mapprod = new HashMap();
				mapprod.put("proId", ((Object[]) (lists.get(i)))[0]);
				mapprod.put("proName", ((Object[]) (lists.get(i)))[1]);
				mapProduct.add(mapprod);

			}
			mapda.put("product", mapProduct);

			Map mapCount = new HashMap();
			Map mapCount1 = new HashMap();
			// 循环多天
			Date st = start;
			for (int j = 0; j < quotas.size(); j++) {
				mapCount.put(quotas.get(j).getQuotaReportNameId() + "",
						countList.get(j));
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String str = sdf.format(st);
			mapCount1.put(str, mapCount);
			st = DateHandler.GetAfterDay(st, 1);

			mapda.put("grandtotal", mapCount1);

			// 拼接查询数据data的json
			for (int i = 0; i < lists.size(); i++) {
				Object[] sai = (Object[]) lists.get(i);
				Map mapdata = new HashMap();
				mapdata.put("proId", sai[0]);
				if (sai != null) {
					for (int j = 0; j < quotas.size() - 1; j++) {

						mapdata.put(quotas.get(j).getQuotaReportNameId() + "",
								sai[j + 2]);

					}
				}
				mapData.add(mapdata);
			}
			mapda.put("list", mapData);

			map.put("data", mapda);
			mapsum.add(map);
		} else {
			Map map = new LinkedHashMap();
			map.put("success", 0);
			map.put("total", 0);
			map.put("message", "查询结果无数据！");
			mapsum.add(map);
		}
		net.sf.json.JSONArray result = net.sf.json.JSONArray.fromObject(mapsum);

		return result.toString();

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String buildMutiDayJSON(List<List> lists, List countList,
			Date start, int period, List<Integer> indexs, int countNum) {
		List<Quotas> quotas = this.quotasServiceImpl.getQuotasIndexs(indexs);
		// 拼接返回json数据字符串
		// mapQuotas代表quotas指标
		List<Map> mapQuotas = new ArrayList<Map>();
		// mapProduct代表产品json
		List<Map> mapProduct = new ArrayList<Map>();
		// mapData代表data
		List<Map> mapData = new ArrayList<Map>();
		// mapDataCount代表合计
		List<Map> mapDataCount = new ArrayList<Map>();
		// 最后的list
		List<Map> mapsum = new ArrayList<Map>();
		if (lists.size() > 0) {
			Map map = new LinkedHashMap();
			map.put("success", 1);
			map.put("total", lists.size());
			map.put("message", "执行成功");
			Map mapda = new HashMap();
			// 拼接指标
			for (int i = 0; i < quotas.size(); i++) {
				Map mapquota = new HashMap();
				mapquota.put("quotasNameId", quotas.get(i)
						.getQuotaReportNameId());
				mapquota.put("quotasName", quotas.get(i).getQuotaName());
				mapQuotas.add(mapquota);

			}
			mapda.put("quotas", mapQuotas);
			List listpro = this.getProIdAndProName();
			// 拼接产品json
			for (int i = 0; i < listpro.size(); i++) {
				Map mapprod = new HashMap();
				mapprod.put("proId", ((Object[]) (listpro.get(i)))[1]);
				mapprod.put("proName", ((Object[]) (listpro.get(i)))[0]);
				mapProduct.add(mapprod);

			}
			mapda.put("product", mapProduct);

			// 拼接合计列
			Map mapCount = new HashMap();
			Map mapCount1 = new HashMap();
			// 循环多天
			Date st = start;
			for (int i = 0; i < countNum; i++) {
				for (int j = 0; j < quotas.size(); j++) {
					mapCount.put(quotas.get(j).getQuotaReportNameId() + "",
							countList.get(quotas.size() * i + j));
				}
				// Date st=start;
				if (period == 2) {

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String str = sdf.format(st);
					mapCount1.put(str, mapCount);
					st = DateHandler.GetAfterDay(st, 1);
				} else if (period == 3) {

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
					String str = sdf.format(st);
					mapCount1.put(str, mapCount);
					st = DateHandler.GetAfterMonth(st, 1);
				}

			}
			mapDataCount.add(mapCount1);
			mapda.put("grandtotal", mapDataCount);

			// 拼接查询数据data的json
			// 多日数据json
			for (int i = 0; i < lists.get(0).size(); i++) {
				Map mapdata = new LinkedHashMap();
				mapdata.put("proId", ((Object[]) (listpro.get(i)))[1]);
				// Map mapdata1 = new LinkedHashMap();
				Date st1 = start;
				// 多日数据拼接
				for (int k = 0; k < lists.size(); k++) {
					List twolist = (List) (lists.get(k)).get(i);
					Map mapdata1 = new LinkedHashMap();
					for (int j = 0; j < quotas.size(); j++) {
						if (j == 7 || j == 8) {
							mapdata1.put(quotas.get(j).getQuotaReportNameId()
									+ "", twolist.get(j) + "%");
						} else {
							mapdata1.put(quotas.get(j).getQuotaReportNameId()
									+ "", twolist.get(j));
						}
					}
					if (period == 2) {

						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MM-dd");
						String str = sdf.format(st1);
						mapdata.put(str, mapdata1);
						st1 = DateHandler.GetAfterDay(st1, 1);
					} else if (period == 3) {

						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
						String str = sdf.format(st1);
						mapdata.put(str, mapdata1);
						st = DateHandler.GetAfterMonth(st1, 1);
					}
				}

				mapData.add(mapdata);

			}
			mapda.put("list", mapData);

			map.put("data", mapda);
			mapsum.add(map);
		} else {
			Map map = new LinkedHashMap();
			map.put("success", 0);
			map.put("total", 0);
			map.put("message", "查询结果无数据！");
			mapsum.add(map);
		}
		net.sf.json.JSONArray result = net.sf.json.JSONArray.fromObject(mapsum);

		return result.toString();
	}

	@SuppressWarnings("rawtypes")
	public String buildOnlineResultMonthDayHTML(Date start, Date end,
			List<List> lists, List<Integer> indexs, List lielist) {

		JSONArray json = JSONArray.fromObject(lists);
		return json.toString();
	}

	// 计算合计
	@SuppressWarnings({ "rawtypes" })
	public List getCount(Date start, List<Integer> indexs) {
		// 全部集合
		StringBuffer sbf = new StringBuffer();
		sbf.append(" SELECT ");
		if (indexs.size() > 0) {
			if (indexs.contains(1)) {
				sbf.append(" T_STORE_USER, ");
			}
			if (indexs.contains(2)) {
				sbf.append(" T_SALES_USER, ");
			}
			if (indexs.contains(3)) {
				sbf.append(" T_BILL_USER, ");
			}
			if (indexs.contains(4)) {
				sbf.append(" T_NEW_USER, ");
			}
			if (indexs.contains(5)) {
				sbf.append(" T_CANCEL_USER, ");
			}
			if (indexs.contains(6)) {
				sbf.append(" T_THREEDAY_CANCEL_USER, ");
			}
			if (indexs.contains(7)) {
				sbf.append(" T_INCREASE_USER, ");
			}
			if (indexs.contains(8)) {
				sbf.append(" ROUND(T_CANCEL_USER*100/(T_STORE_USER+T_THREEDAY_CANCEL_USER),2) AS L_CANCEL_PERCENT, ");
			}
			if (indexs.contains(9)) {
				sbf.append(" ROUND(T_INCREASE_USER*100/T_NEW_USER,2) AS L_EXIST_PERCENT,");
			}
		}
		sbf.deleteCharAt(sbf.length() - 1);
		sbf.append(" from ( ");

		sbf.append(" SELECT sum(A.T_STORE_USER) as T_STORE_USER  ,sum(A.T_SALES_USER) as T_SALES_USER,sum(A.T_BILL_USER) as T_BILL_USER,");
		sbf.append("sum(A.T_NEW_USER)  as T_NEW_USER,sum(A.T_CANCEL_USER) as T_CANCEL_USER,sum(A.T_THREEDAY_CANCEL_USER)  as T_THREEDAY_CANCEL_USER, ");
		sbf.append(" sum(A.T_INCREASE_USER)  as T_INCREASE_USER from C_ONLINEUSER_PRODUCT A  WHERE ");

		sbf.append(" A.T_GATHER_TIME = ? )");
		String sql = sbf.toString();
		List listss = null;
		try {

			listss = dao.findBySql(sql, new Object[] { start });
			// listss = dao.findBySql(sql, null);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return listss;
	}

	// 计算合计按月的
	@SuppressWarnings({ "rawtypes" })
	public List getMonthCount(Date start, Date end, List<Integer> indexs) {
		// 全部集合
		StringBuffer sbf = new StringBuffer();
		String dayType = "yyyy-MM-dd";
		SimpleDateFormat sdf1 = new SimpleDateFormat(dayType);
		sbf.append(" SELECT ");
		if (indexs.size() > 0) {
			if (indexs.contains(1)) {
				sbf.append(" ST, ");
			}
			if (indexs.contains(2)) {
				sbf.append(" ORD, ");
			}
			if (indexs.contains(3)) {
				sbf.append(" BILL, ");
			}
			if (indexs.contains(4)) {
				sbf.append(" NEWCOME, ");
			}
			if (indexs.contains(5)) {
				sbf.append(" CANCE, ");
			}
			if (indexs.contains(6)) {
				sbf.append(" THREECANCE, ");
			}
			if (indexs.contains(7)) {
				sbf.append(" INCOME, ");
			}
			if (indexs.contains(8)) {
				sbf.append(" ROUND(CANCE*100/(ST+THREECANCE),2) AS L_CANCEL_PERCENT,");
			}
			if (indexs.contains(9)) {
				sbf.append("ROUND(INCOME*100/NEWCOME, 2) AS L_EXIST_PERCENT,");
			}
			if (indexs.contains(10)) {
				sbf.append("L_IN,");
			}
		}
		sbf.deleteCharAt(sbf.length() - 1);
		sbf.append(" FROM ( ");
		sbf.append(" SELECT SUM(存量用户) AS ST ,SUM(订购用户) AS ORD,SUM(计费用户) AS BILL,SUM(新增用户)AS NEWCOME, ");
		sbf.append(" SUM(退订用户) AS CANCE,SUM(三日退订用户) AS THREECANCE,SUM(净增用户) AS INCOME,SUM(收入) AS L_IN ");
		sbf.append(" FROM ( ");
		sbf.append(" SELECT T_PACK_ID ,T_PRODUCT_NAME  ,存量用户   ,订购用户  , 计费用户  ,新增用户  ,退订用户  ,三日退订用户  ,净增用户  ,流失率 ,留存率  ,(存量用户+退订用户-三日退订用户)*价格 as 收入  ");
		sbf.append(" FROM ( ");
		sbf.append(" SELECT  M.T_PACK_ID, M.T_PRODUCT_NAME ,");
		sbf.append(" (SELECT S.T_STORE_USER  FROM (SELECT *  FROM (SELECT ROW_NUMBER() OVER(PARTITION BY AA.T_PRODUCT_NAME ORDER BY AA.T_GATHER_TIME DESC) RN,   AA.* ");
		sbf.append(" FROM C_ONLINEUSER_PRODUCT AA WHERE 1=1 and ");
		sbf.append(" TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') >='"
				+ sdf1.format(start) + "' ");
		sbf.append(" AND TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') <'"
				+ sdf1.format(end) + "' ");
		sbf.append(" ) A WHERE A.RN = 1) S ");
		sbf.append("  WHERE S.T_PRODUCT_NAME = M.T_PRODUCT_NAME) AS 存量用户,");
		sbf.append(" (SELECT S.T_SALES_USER  FROM (SELECT *  FROM (SELECT ROW_NUMBER() OVER(PARTITION BY AA.T_PRODUCT_NAME ORDER BY AA.T_GATHER_TIME DESC) RN,   AA.* ");
		sbf.append(" FROM C_ONLINEUSER_PRODUCT AA WHERE 1=1 and ");
		sbf.append(" TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') >='"
				+ sdf1.format(start) + "' ");
		sbf.append(" AND TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') <'"
				+ sdf1.format(end) + "' ");
		sbf.append(" ) A WHERE A.RN = 1) S ");
		sbf.append("  WHERE S.T_PRODUCT_NAME = M.T_PRODUCT_NAME) AS 订购用户,");

		sbf.append(" (SELECT S.T_BILL_USER  FROM (SELECT *  FROM (SELECT ROW_NUMBER() OVER(PARTITION BY AA.T_PRODUCT_NAME ORDER BY AA.T_GATHER_TIME DESC) RN,   AA.* ");
		sbf.append(" FROM C_ONLINEUSER_PRODUCT AA WHERE 1=1 and ");
		sbf.append(" TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') >='"
				+ sdf1.format(start) + "' ");
		sbf.append(" AND TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') <'"
				+ sdf1.format(end) + "' ");
		sbf.append(" ) A WHERE A.RN = 1) S ");
		sbf.append("  WHERE S.T_PRODUCT_NAME = M.T_PRODUCT_NAME) AS 计费用户,");

		sbf.append(" SUM(M.T_NEW_USER) AS 新增用户, SUM(M.T_CANCEL_USER) AS 退订用户,SUM(M.T_THREEDAY_CANCEL_USER) AS 三日退订用户, SUM(M.T_INCREASE_USER) AS 净增用户,");
		sbf.append(" (SELECT L.T_LOSS_PERCENT   FROM (SELECT * FROM (SELECT ROW_NUMBER() OVER(PARTITION BY AA.T_PRODUCT_NAME ORDER BY AA.T_GATHER_TIME DESC) RN,  AA.* ");
		sbf.append(" FROM C_ONLINEUSER_PRODUCT AA WHERE 1=1 and ");
		sbf.append(" TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') >='"
				+ sdf1.format(start) + "' ");
		sbf.append(" AND TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') <'"
				+ sdf1.format(end) + "' ");
		sbf.append(") A WHERE A.RN = 1) L WHERE L.T_PRODUCT_NAME = M.T_PRODUCT_NAME) AS 流失率,");
		sbf.append("  (SELECT K.T_EXIST_PERCENT  FROM (SELECT *  FROM (SELECT ROW_NUMBER() OVER(PARTITION BY AA.T_PRODUCT_NAME ORDER BY AA.T_GATHER_TIME DESC) RN, AA.*  ");
		sbf.append(" FROM C_ONLINEUSER_PRODUCT AA WHERE 1=1 and ");
		sbf.append(" TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') >='"
				+ sdf1.format(start) + "' ");
		sbf.append(" AND TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') <'"
				+ sdf1.format(end) + "' ");
		sbf.append(" ) A  WHERE A.RN = 1) K WHERE K.T_PRODUCT_NAME = M.T_PRODUCT_NAME) AS 留存率,M.T_PRODUCT_PRICE AS 价格 ");
		sbf.append(" FROM C_ONLINEUSER_PRODUCT M WHERE 1=1 and ");
		sbf.append(" TO_CHAR(M.T_GATHER_TIME, 'yyyy-mm-dd') >='"
				+ sdf1.format(start) + "' ");
		sbf.append(" AND TO_CHAR(M.T_GATHER_TIME, 'yyyy-mm-dd') <'"
				+ sdf1.format(end) + "' ");
		sbf.append("   GROUP BY M.T_PRODUCT_NAME, M.T_PACK_ID, M.T_PRODUCT_PRICE ");
		sbf.append("   ORDER BY M.T_PRODUCT_NAME )))");

		String sql = sbf.toString();
		List listss = null;
		try {
			listss = dao.findBySql(sql, null);
			// listss = dao.findBySql(sql, null);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return listss;
	}

	@SuppressWarnings({ "rawtypes" })
	public List getData(Date start, List<Integer> indexs) {
		// 全部集合
		StringBuffer sbf = new StringBuffer();
		sbf.append(" SELECT A.T_PACK_ID,A.T_PRODUCT_NAME ");
		if (indexs.size() > 0) {
			if (indexs.contains(1)) {
				sbf.append(" ,A.T_STORE_USER ");
			}
			if (indexs.contains(2)) {
				sbf.append("  ,A.T_SALES_USER ");
			}
			if (indexs.contains(3)) {
				sbf.append(" ,A.T_BILL_USER ");
			}
			if (indexs.contains(4)) {
				sbf.append(" ,A.T_NEW_USER ");
			}
			if (indexs.contains(5)) {
				sbf.append(" ,A.T_CANCEL_USER ");
			}
			if (indexs.contains(6)) {
				sbf.append(" ,A.T_THREEDAY_CANCEL_USER ");
			}
			if (indexs.contains(7)) {
				sbf.append(" ,A.T_INCREASE_USER ");
			}
			if (indexs.contains(8)) {
				sbf.append(" ,A.T_LOSS_PERCENT ");
			}
			if (indexs.contains(9)) {
				sbf.append(" ,A.T_EXIST_PERCENT ");
			}
		}
		sbf.append(" from C_ONLINEUSER_PRODUCT A WHERE   ");
		// sbf.append(" A.T_GATHER_TIME = to_date('2016-10-05','yyyy-mm-dd')");
		sbf.append(" A.T_GATHER_TIME = ? ");
		sbf.append("  ORDER BY A.T_PRODUCT_NAME  ");
		String sql = sbf.toString();
		List listss = null;
		try {
			listss = dao.findBySql(sql, new Object[] { start });
			// listss = dao.findBySql(sql, null);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return listss;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<List> getMonthData(Date start, Date end, List<Integer> indexs,
			int period) {
		String dayType = "yyyy-MM-dd";
		SimpleDateFormat sdf1 = new SimpleDateFormat(dayType);

		StringBuffer sbf = new StringBuffer();
		sbf.append(" SELECT T_PACK_ID ");
		sbf.append(", T_PRODUCT_NAME ");
		if (indexs.size() > 0) {
			if (indexs.contains(1)) {
				sbf.append(" ,存量用户 ");
			}
			if (indexs.contains(2)) {
				sbf.append("  ,订购用户 ");
			}
			if (indexs.contains(3)) {
				sbf.append(" ,计费用户 ");
			}
			if (indexs.contains(4)) {
				sbf.append(" ,新增用户 ");
			}
			if (indexs.contains(5)) {
				sbf.append(" ,退订用户 ");
			}
			if (indexs.contains(6)) {
				sbf.append(" ,三日退订用户 ");
			}
			if (indexs.contains(7)) {
				sbf.append(" ,净增用户 ");
			}
			if (indexs.contains(8)) {
				sbf.append(" ,流失率");
			}
			if (indexs.contains(9)) {
				sbf.append(" ,留存率 ");
			}
			if (indexs.contains(10)) {
				sbf.append(" ,(存量用户+退订用户-三日退订用户)*价格 as 收入  ");
			}
		}
		sbf.append(" FROM( SELECT M.T_PACK_ID,M.T_PRODUCT_NAME ,");
		sbf.append(" (SELECT S.T_STORE_USER  FROM (SELECT *  FROM (SELECT ROW_NUMBER() OVER(PARTITION BY AA.T_PRODUCT_NAME ORDER BY AA.T_GATHER_TIME DESC) RN,   AA.* ");
		sbf.append(" FROM C_ONLINEUSER_PRODUCT AA WHERE 1=1 and ");
		sbf.append(" TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') >='"
				+ sdf1.format(start) + "' ");
		sbf.append(" AND TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') <'"
				+ sdf1.format(end) + "' ");
		sbf.append(" ) A WHERE A.RN = 1) S ");
		sbf.append("  WHERE S.T_PRODUCT_NAME = M.T_PRODUCT_NAME) AS 存量用户,");
		sbf.append(" (SELECT S.T_SALES_USER  FROM (SELECT *  FROM (SELECT ROW_NUMBER() OVER(PARTITION BY AA.T_PRODUCT_NAME ORDER BY AA.T_GATHER_TIME DESC) RN,   AA.* ");
		sbf.append(" FROM C_ONLINEUSER_PRODUCT AA WHERE 1=1 and ");
		sbf.append(" TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') >='"
				+ sdf1.format(start) + "' ");
		sbf.append(" AND TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') <'"
				+ sdf1.format(end) + "' ");
		sbf.append(" ) A WHERE A.RN = 1) S ");
		sbf.append("  WHERE S.T_PRODUCT_NAME = M.T_PRODUCT_NAME) AS 订购用户,");

		sbf.append(" (SELECT S.T_BILL_USER  FROM (SELECT *  FROM (SELECT ROW_NUMBER() OVER(PARTITION BY AA.T_PRODUCT_NAME ORDER BY AA.T_GATHER_TIME DESC) RN,   AA.* ");
		sbf.append(" FROM C_ONLINEUSER_PRODUCT AA WHERE 1=1 and ");
		sbf.append(" TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') >='"
				+ sdf1.format(start) + "' ");
		sbf.append(" AND TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') <'"
				+ sdf1.format(end) + "' ");
		sbf.append(" ) A WHERE A.RN = 1) S ");
		sbf.append("  WHERE S.T_PRODUCT_NAME = M.T_PRODUCT_NAME) AS 计费用户,");

		sbf.append(" SUM(M.T_NEW_USER) AS 新增用户, SUM(M.T_CANCEL_USER) AS 退订用户,SUM(M.T_THREEDAY_CANCEL_USER) AS 三日退订用户, SUM(M.T_INCREASE_USER) AS 净增用户,");
		sbf.append(" (SELECT L.T_LOSS_PERCENT   FROM (SELECT * FROM (SELECT ROW_NUMBER() OVER(PARTITION BY AA.T_PRODUCT_NAME ORDER BY AA.T_GATHER_TIME DESC) RN,  AA.* ");
		sbf.append(" FROM C_ONLINEUSER_PRODUCT AA WHERE 1=1 and ");
		sbf.append(" TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') >='"
				+ sdf1.format(start) + "' ");
		sbf.append(" AND TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') <'"
				+ sdf1.format(end) + "' ");
		sbf.append(") A WHERE A.RN = 1) L WHERE L.T_PRODUCT_NAME = M.T_PRODUCT_NAME) AS 流失率,");
		sbf.append("  (SELECT K.T_EXIST_PERCENT  FROM (SELECT *  FROM (SELECT ROW_NUMBER() OVER(PARTITION BY AA.T_PRODUCT_NAME ORDER BY AA.T_GATHER_TIME DESC) RN, AA.*  ");
		sbf.append(" FROM C_ONLINEUSER_PRODUCT AA WHERE 1=1 and ");
		sbf.append(" TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') >='"
				+ sdf1.format(start) + "' ");
		sbf.append(" AND TO_CHAR(AA.T_GATHER_TIME, 'yyyy-mm-dd') <'"
				+ sdf1.format(end) + "' ");
		sbf.append(" ) A  WHERE A.RN = 1) K WHERE K.T_PRODUCT_NAME = M.T_PRODUCT_NAME) AS 留存率,M.T_PRODUCT_PRICE AS 价格 ");
		sbf.append(" FROM C_ONLINEUSER_PRODUCT M WHERE 1=1 and ");
		sbf.append(" TO_CHAR(M.T_GATHER_TIME, 'yyyy-mm-dd') >='"
				+ sdf1.format(start) + "' ");
		sbf.append(" AND TO_CHAR(M.T_GATHER_TIME, 'yyyy-mm-dd') <'"
				+ sdf1.format(end) + "' ");
		sbf.append("   GROUP BY M.T_PRODUCT_NAME, M.T_PACK_ID, M.T_PRODUCT_PRICE ");
		sbf.append("   ORDER BY M.T_PRODUCT_NAME )");
		String sql = sbf.toString();
		List listss = null;
		try {
			listss = dao.findBySql(sql, null);

		} catch (ServiceException e) {

			e.printStackTrace();
		}

		return listss;
	}

	/*
	 * 计算产品列
	 */
	@SuppressWarnings({ "rawtypes" })
	public List getLie() {
		// 全部集合
		List lists = new ArrayList();

		StringBuffer sbf = new StringBuffer();
		sbf.append(" SELECT DISTINCT A.T_PRODUCT_NAME  ");
		sbf.append("FROM C_ONLINEUSER_PRODUCT A ");
		sbf.append("  ORDER BY A.T_PRODUCT_NAME  ");
		String sql = sbf.toString();
		try {
			lists = dao.findBySql(sql, null);

		} catch (ServiceException e) {

			e.printStackTrace();
		}
		return lists;
	}

	@SuppressWarnings({ "rawtypes" })
	public List getProIdAndProName() {
		// 全部集合
		List lists = new ArrayList();

		StringBuffer sbf = new StringBuffer();
		sbf.append(" SELECT DISTINCT A.T_PRODUCT_NAME ,A.T_PACK_ID ");
		sbf.append("FROM C_ONLINEUSER_PRODUCT A ");
		sbf.append("  ORDER BY A.T_PRODUCT_NAME  ");
		String sql = sbf.toString();
		try {
			lists = dao.findBySql(sql, null);

		} catch (ServiceException e) {

			e.printStackTrace();
		}
		return lists;
	}
}
