package com.ctid.business.online.service.impl;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import com.ctid.business.online.service.MarketingChannelService;
import com.ctid.core.exception.ServiceException;
import com.ctid.core.hibernate.IHibernateDaoSys;
import com.ctid.util.DateHandler;
/**
 * 营销渠道总表实现类
 * 
 * @author 杜文雅
 * @date 2015-8-24
 *
 */
@Service(value = "MarketingChannelServiceImpl")
public class MarketingChannelServiceImpl implements MarketingChannelService {
	Log logger = LogFactory.getLog(MarketingChannelServiceImpl.class);
	
	@Resource
	private IHibernateDaoSys dao;

	/**
	 * 获取在渠道总表单日统计的结果
	 * @param date  查询日期
	 * @param indexs 查询指标
	 * @return 数据表格HTML字符串
	 */
	@Override
	public String getMarketingChannelSingleDay(Date date, List<Integer> indexs) {

		Date start = date;

		Date end = DateHandler.GetAfterDay(start, 1);

		Date now = new Date();

		if (now.before(end)) {

			end = now;

		} else {

			end = DateHandler.GetAfterSecond(end, -1);
		}

		try {

			List<List> lists = this.getData(start, indexs, 1);
			List lielist=this.getLie();
			if(lists==null||lists.get(0).size()<=3){
				String str="<table id=\"tbdatatableww\" style=\"width:1080px;\"><tr><td><div class=\"pp\">查询时间段无数据</div></tr></table>";
				return str;
			}else{
				String resultHtml = this.buildSalesResultSingleDayHTML(start, end, lists, indexs, lielist);


				return resultHtml;
			}
			

		} catch (Exception e) {

			e.printStackTrace();


			return "";
		}
	}
	
	/**
	 * 获取在渠道总表多日统计的结果
	 * @param date  查询日期
	 * @param indexs 查询指标
	 * @return 数据表格HTML字符串
	 */
	@Override
	public String getMarketingChannelMutiDay(Date start, Date end, List<Integer> indexs){
		end = DateHandler.GetAfterDay(end, 1);

		Date now = new Date();

		if (now.before(end)) {

			end = now;

		} else {

			end = DateHandler.GetAfterSecond(end, -1);
		}

		List<List> lists = new ArrayList<List>();
		List lielist=this.getLie();
		Date st = start;

		
		while (st.before(end)) {
			List list=this.getData(st, indexs, 2);
			ArrayList lis=(ArrayList) list.get(0);
			if(lis.size()<3){
				lists.add(null);
			} else{
				 lists.addAll(list);
			}
			
			//lists=this.getData(st, ed, indexs, period);
			st = DateHandler.GetAfterDay(st, 1);

		}
		
		
		if(lists.contains(null)){
			String str="<table id=\"tbdatatableww\" style=\"width:1080px;\"><tr><td><div class=\"pp\">查询时间段无数据</div></tr></table>";
			return str;
		}else{
			String resultHtml = this.buildSalesResultMutiDayHTML(start, end,
					lists, indexs,lielist);


			return resultHtml;
		}
		
		
	}
	/**
	 * 获取在渠道总表按月统计的结果
	 * @param date  查询日期
	 * @param indexs 查询指标
	 * @return 数据表格HTML字符串
	 */
	@Override
	public String getMarketingChannelMonth(int startYear, int startMonth, int endYear,
			int endMonth, List<Integer> indexs) {
//		startYear=2014;
//		startMonth=11;
//		endYear=2015;
//		endMonth=1;
		Date start = DateHandler.GetDate(startYear, startMonth,1);
		Date end = DateHandler.GetDate(endYear, endMonth, 1);

		end = DateHandler.GetAfterMonth(end, 1);

		Date now = new Date();

		if (now.before(end)) {

			end = now;

		} else {

			end = DateHandler.GetAfterSecond(end, -1);
		}

		List<List> lists = new ArrayList<List>();
		List lielist=this.getLie();
		Date st = start;

		Date ed = DateHandler.GetAfterMonth(st, 1);

		while (st.before(end)) {
			List list=this.getMonthData(st, ed, indexs, 3);
			ArrayList lis=(ArrayList) list.get(0);
			if(lis.size()<3){
				lists.add(null);
			}else{
				lists.addAll(list);
			}
//			lists.addAll(this.getMonthData(st, ed,indexs, period));

			st = DateHandler.GetAfterMonth(st, 1);
			ed = DateHandler.GetAfterMonth(ed, 1);
		}
		
		
		if(lists.contains(null)){
			String str="<table id=\"tbdatatableww\" style=\"width:1080px;\"><tr><td><div class=\"pp\">查询时间段无数据</div></tr></table>";
			return str;
		}else{
			String resultHtml = this.buildSalesResultMonthDayHTML(start,

					end, lists, indexs,lielist);

					return resultHtml;
		}
		
		
	}
	
	
	/**
	 * 获取在网用户下分省总表单日统计的HTML字符串
	 * 
	 * @param start
	 *            查询周期开始时间
	 * @param end
	 *            查询周期结束时间
	 * @param lists
	 *            表格每列列表
	 * @param indexs
	 *            查询指标
	 * @return HTML字符串
	 */
	private String buildSalesResultSingleDayHTML(Date start, Date end,
			List<List> lists, List<Integer> indexs,List lielist) {

		StringBuffer sbf = new StringBuffer();

		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy 年 MM 月  dd 日");

		sbf.append("<table id=\"tbdatatable2\" style=\"width:1080px;\" >");

		sbf.append("<tr><td><div class=\"pp\">日期</div></td>");

		Date st1 = start;
		Date st2 = start;
		while (st1.before(end)) {
			if (indexs.size() == 10) {
				sbf.append("<td colspan=\"" + (indexs.size() - 1)
						+ "\"><div class=\"pp\">" + sdf1.format(st1)
						+ "</div></td>");
			} else {
				sbf.append("<td colspan=\"" + (indexs.size())
						+ "\"><div class=\"pp\">" + sdf1.format(st1)
						+ "</div></td>");
			}
			st1 = DateHandler.GetAfterDay(st1, 1);
		}

		sbf.append("</tr>");

		for (int i = 0; i < lists.get(0).size(); i++) {

			
			
			sbf.append("<tr> ");
			sbf.append("<td><div class=\"pp\">");
			sbf.append(lielist.get(i));
			sbf.append("</div></td>");
			
				for (int j = 0; j < lists.size(); j++) {
					
					sbf.append("<td><div class=\"pp\">");
					sbf.append(lists.get(j).get(i));
					sbf.append("</div></td>");
				}
				
		

		sbf.append("</tr>");
	}

		sbf.append("</table>");

		String resultHtml = sbf.toString();

		return resultHtml;
	}
	
	
	public String buildSalesResultMutiDayHTML(Date start, Date end,
			List<List> lists, List<Integer> indexs,List lielist) {

		StringBuffer sbf = new StringBuffer();

		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy 年 MM 月  dd 日");

		sbf.append("<table id=\"tbdatatable2\" style=\"width:1080px;\" >");

		sbf.append("<tr><td><div class=\"pp\">日期</div></td>");

		Date st1 = start;
		Date st2 = start;
		while (st1.before(end)) {
			
				sbf.append("<td colspan=\"" + indexs.size()
						+ "\"><div class=\"pp\">" + sdf1.format(st1)
						+ "</div></td>");
			
			st1 = DateHandler.GetAfterDay(st1, 1);
		}

		sbf.append("</tr>");

		for (int i = 0; i < lists.get(0).size(); i++) {

			
			
			sbf.append("<tr> ");
			sbf.append("<td><div class=\"pp\">");
			sbf.append(lielist.get(i));
			sbf.append("</div></td>");
			
				for (int j = 0; j < lists.size(); j++) {
					
					sbf.append("<td><div class=\"pp\">");
					sbf.append(lists.get(j).get(i));
					sbf.append("</div></td>");
				}
				
		

		sbf.append("</tr>");
	}

		sbf.append("</table>");

		String resultHtml = sbf.toString();

		return resultHtml;
	}

	public String buildSalesResultMonthDayHTML(Date start, Date end,
			List<List> lists, List<Integer> indexs,List lielist) {

		StringBuffer sbf = new StringBuffer();

		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy 年 MM 月   ");

		sbf.append("<table id=\"tbdatatable2\" style=\"width:1080px;\">");

		sbf.append("<tr><td><div class=\"pp\">日期<div></td>");

		Date st1 = start;

		while (st1.before(end)) {

			sbf.append("<td colspan=\"" + indexs.size()
					+ "\"><div class=\"pp\">" + sdf1.format(st1)
					+ "</div></td>");

			st1 = DateHandler.GetAfterMonth(st1, 1);
		}

		sbf.append("</tr>");

		for (int i = 0; i < lists.get(0).size(); i++) {

			
			
			sbf.append("<tr> ");
			sbf.append("<td><div class=\"pp\">");
			sbf.append(lielist.get(i));
			sbf.append("</div></td>");
			
				for (int j = 0; j < lists.size(); j++) {
					
					sbf.append("<td><div class=\"pp\">");
					sbf.append(lists.get(j).get(i));
					sbf.append("</div></td>");
				}
				
		

		sbf.append("</tr>");
	}

		sbf.append("</table>");

		String resultHtml = sbf.toString();

		return resultHtml;
	}
	
	/**
	 * 获取在网用户下分省总表单日统计的数据
	 * 
	 * @param start
	 *            查询周期开头
	 * @param end
	 *            查询周期结尾
	 * @param indexs
	 *            指标
	 * @param times
	 *            第几次调用此函数，处理多日多月添加一次省份
	 * @return 数据列表
	 */
	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public List<List> getData(Date start, List<Integer> indexs,
			int period) {
		// 全部集合
		List<List> lists = new ArrayList<List>();

		StringBuffer sbf = new StringBuffer();

		sbf.append(" SELECT A.T_COVER_USER,A.T_REACH_USER,A.T_NEW_USER, ");
		sbf.append(" A.T_CANCEL_USER,A.T_THREEDAY_CANCEL,A.T_INCOME_USER,");
		sbf.append(" A.T_CHANGE_PERCENT,A.T_EXISTS_PERCENT FROM WX.C_CHANNELSALE_CHANNEL A WHERE ");
		sbf.append(" A.T_GATHER_TIME = ?");
		sbf.append(" ORDER BY A.T_CHANNEL_NAME ");
		
		String sql = sbf.toString();

		List<Object[]> listsome = new ArrayList<Object[]>();

		listsome.add(new Object[] {"覆盖用户（个）","到达用户（个）","新增用户（个）",  "退订用户", "三日退订用户", "净增用户", "订购转化率（%）", "留存率（%）", });

		try {
			List listss = dao.findBySql(sql, new Object[] {start});
			if (listss != null) {

				listsome.addAll(listss);
			}
		} catch (ServiceException e) {

			e.printStackTrace();
		}

		List<Object> coveruser = new ArrayList<Object>();
		List<Object> daouser = new ArrayList<Object>();
		List<Object> newuser = new ArrayList<Object>();
		List<Object> returnuser = new ArrayList<Object>();
		List<Object> returnIn3user = new ArrayList<Object>();
		List<Object> returnjing = new ArrayList<Object>();
		List<Object> changepercent = new ArrayList<Object>();
		List<Object> exitpercent = new ArrayList<Object>();
		

		for (int i = 0; i < listsome.size(); i++) {
			
			// 存量用户
			coveruser.add(i, listsome.get(i)[0]);
			// 到达用户
			daouser.add(i, listsome.get(i)[1]);
			// 新增用户
			newuser.add(i, listsome.get(i)[2]);
			// 退订用户
			returnuser.add(i, listsome.get(i)[3]);
			// 三日退订用户
			returnIn3user.add(i, listsome.get(i)[4]);
			// 净增用户
			returnjing.add(i, listsome.get(i)[5]);
			// 流失率
			changepercent.add(i, listsome.get(i)[6]);
			// 留存率
			exitpercent.add(i, listsome.get(i)[7]);
			
		}

		

		// 存量总计
		int countonline = 0;

		for (int i = 1; i < coveruser.size(); i++) {

			int i1 = Integer.parseInt(coveruser.get(i).toString());

			countonline = countonline + i1;
		}

		coveruser.add(countonline);
		// 到达用户总计
				int countdaouser = 0;

				for (int i = 1; i < daouser.size(); i++) {

					int i2 = Integer.parseInt(daouser.get(i).toString());

					countdaouser = countdaouser + i2;
				}

				daouser.add(countdaouser);
		// 新增总计
		int countnewuser = 0;

		for (int i = 1; i < newuser.size(); i++) {

			int i4 = Integer.parseInt(newuser.get(i).toString());

			countnewuser = countnewuser + i4;
		}

		newuser.add(countnewuser);
		// 退订总计
		int countreturnuser = 0;

		for (int i = 1; i < returnuser.size(); i++) {

			int i5 = Integer.parseInt(returnuser.get(i).toString());

			countreturnuser = countreturnuser + i5;
		}

		returnuser.add(countreturnuser);
		// 三日退订总计
		int countreturnIn3 = 0;

		for (int i = 1; i < returnIn3user.size(); i++) {

			Integer i6 = new Integer(returnIn3user.get(i).toString());

			countreturnIn3 = countreturnIn3 + i6;
		}

		returnIn3user.add(countreturnIn3);
		// 净增用户
		List<Object> realAdd = new ArrayList<Object>();

		realAdd.add("净增用户（个）");

		for (int i = 1; i < newuser.size(); i++) {

			Integer newadd = Integer.parseInt(newuser.get(i).toString());

			Integer returnadd = Integer.parseInt(returnuser.get(i).toString());

			Integer real = newadd - returnadd;

			realAdd.add(real);
		}
		// 订购转化率（%）
		List<Object> loseRate = new ArrayList<Object>();

		loseRate.add("订购转化率（%）");

		for (int i = 1; i < coveruser.size(); i++) {

			BigDecimal b1 = new BigDecimal(daouser.get(i).toString());

			BigDecimal b2 = new BigDecimal(newuser.get(i).toString());

			

			Double d3 = 0.0000;

			if (b1.doubleValue() != 0) {

				d3 = b2.divide(b1, 6, BigDecimal.ROUND_HALF_UP).doubleValue();
			}

			NumberFormat nf = NumberFormat.getPercentInstance();

			nf.setMaximumFractionDigits(2);

			loseRate.add(nf.format(d3));
		}
		// 存留率
		List<Object> retentionRate = new ArrayList<Object>();

		retentionRate.add("留存率（%）");

		for (int i = 1; i < realAdd.size(); i++) {

			BigDecimal b1 = new BigDecimal(returnIn3user.get(i).toString());

			BigDecimal b2 = new BigDecimal(newuser.get(i).toString());
			BigDecimal b3 =b2.subtract(b1); 
			double d3 = 0.0000;

			if (b2.doubleValue() != 0 && b3.doubleValue() > 0) {

				d3 = b3.divide(b2, 6, BigDecimal.ROUND_HALF_UP).doubleValue();
			}

			NumberFormat nf = NumberFormat.getPercentInstance();

			nf.setMaximumFractionDigits(2);

			retentionRate.add(nf.format(d3));
		}
		


		if (indexs.contains(1)) {

			lists.add(coveruser);
		}
		if (indexs.contains(2)) {

			lists.add(daouser);
		}
		if (indexs.contains(3)) {

			lists.add(newuser);
		}
		if (indexs.contains(4)) {

			lists.add(returnuser);
		}

		if (indexs.contains(5)) {

			lists.add(returnIn3user);
		}

		if (indexs.contains(6)) {

			lists.add(realAdd);
		}

		if (indexs.contains(7)) {

			lists.add(loseRate);
		}

		if (indexs.contains(8)) {

			lists.add(retentionRate);
		}
		return lists;
	}
	@SuppressWarnings("unchecked")
	public List<List> getMonthData(Date start,Date end, List<Integer> indexs,
			int period) {
		// 全部集合
		List<List> lists = new ArrayList<List>();
		String dayType = "yyyy-MM-dd";
		SimpleDateFormat sdf1 = new SimpleDateFormat(dayType);
		StringBuffer sbf = new StringBuffer();
		
		sbf.append("SELECT COVER,REACH,NEWUSER,CANCELUSER,INCOME_USER,THREEDAY_CANCEL, ");
		sbf.append("  decode(nvl(REACH, 0), 0, 0, (NEWUSER * 100 /REACH)) AS 订购转化率, ");
		sbf.append("  decode(sign((NVL(NEWUSER, 0) - NVL(THREEDAY_CANCEL, 0))),-1,0, ");
		sbf.append(" decode(nvl(NEWUSER, 0), 0, 0, (NVL(NEWUSER, 0) - NVL(THREEDAY_CANCEL, 0)) * 100 /NEWUSER)) AS 留存率  ");
		sbf.append("  FROM (  SELECT   A.T_CHANNEL_NAME, sum(A.T_COVER_USER) as COVER, sum(A.T_REACH_USER) as REACH,sum(A.T_NEW_USER) as NEWUSER,");
		sbf.append(" sum(A.T_CANCEL_USER) as CANCELUSER,sum(A.T_INCOME_USER) as INCOME_USER,  sum(A.T_THREEDAY_CANCEL) as THREEDAY_CANCEL ");
		sbf.append("  FROM WX.C_CHANNELSALE_CHANNEL A  WHERE ");
		sbf.append(" TO_CHAR(A.T_GATHER_TIME, 'yyyy-mm-dd') >='"+ sdf1.format(start) + "' ");
		sbf.append(" AND TO_CHAR(A.T_GATHER_TIME, 'yyyy-mm-dd') <'"+ sdf1.format(end) + "' ");
		/*sbf.append(" TO_CHAR(A.T_GATHER_TIME, 'yyyy-mm-dd') >='2014-11-01' ");
		sbf.append(" AND TO_CHAR(A.T_GATHER_TIME, 'yyyy-mm-dd') <'2014-12-01' ");*/
		sbf.append(" group by A.T_CHANNEL_NAME ORDER BY A.T_CHANNEL_NAME )");
		sbf.append(" order by T_CHANNEL_NAME ");
		String sql = sbf.toString();

		List<Object[]> listsome = new ArrayList<Object[]>();

		listsome.add(new Object[] {"覆盖用户（个）","到达用户（个）","新增用户（个）", "退订用户", "净增用户", "三日退订用户","订购转化率", "留存率", });

		try {
			List listss=dao.findBySql(sql, null);
			if (listss != null) {

				listsome.addAll(listss);
			}
		} catch (ServiceException e) {

			e.printStackTrace();
		}

		List<Object> online = new ArrayList<Object>();
		List<Object> reachuser = new ArrayList<Object>();
		List<Object> newuser = new ArrayList<Object>();
		List<Object> returnuser = new ArrayList<Object>();
		List<Object> returnIn3user = new ArrayList<Object>();
		List<Object> returnjing = new ArrayList<Object>();
		List<Object> returnliushi = new ArrayList<Object>();
		List<Object> returnliucun = new ArrayList<Object>();
		for (int i = 0; i < listsome.size(); i++) {
			
			// 覆盖用户
			online.add(i, listsome.get(i)[0]);
			// 到达用户
			reachuser.add(i, listsome.get(i)[1]);
			// 新增用户
			newuser.add(i, listsome.get(i)[2]);
			// 退订用户
			returnuser.add(i, listsome.get(i)[3]);
			
			// 净增用户
			returnjing.add(i, listsome.get(i)[4]);
			// 三日退订用户
			returnIn3user.add(i, listsome.get(i)[5]);
			// 订购转化率
			returnliushi.add(i, listsome.get(i)[6]);
			// 留存率
			returnliucun.add(i, listsome.get(i)[7]);
		}
	
		// 存量总计
		int countonline = 0;

		for (int i = 1; i < online.size(); i++) {

			int i1 = Integer.parseInt(online.get(i).toString());

			countonline = countonline + i1;
		}

		online.add(countonline);
		// 到达总计
		int countreach = 0;
		for (int i = 1; i < reachuser.size(); i++) {

			int i2 = Integer.parseInt(reachuser.get(i).toString());

			countreach = countreach + i2;
		}

		reachuser.add(countreach);
		// 新增总计
		int countnewuser = 0;

		for (int i = 1; i < newuser.size(); i++) {

			int i4 = Integer.parseInt(newuser.get(i).toString());

			countnewuser = countnewuser + i4;
		}

		newuser.add(countnewuser);
		// 退订总计
		int countreturnuser = 0;

		for (int i = 1; i < returnuser.size(); i++) {

			int i5 = Integer.parseInt(returnuser.get(i).toString());

			countreturnuser = countreturnuser + i5;
		}

		returnuser.add(countreturnuser);
		// 三日退订总计
		int countreturnIn3 = 0;

		for (int i = 1; i < returnIn3user.size(); i++) {

			Integer i6 = new Integer(returnIn3user.get(i).toString());

			countreturnIn3 = countreturnIn3 + i6;
		}

		returnIn3user.add(countreturnIn3);
		// 净增用户
		List<Object> realAdd = new ArrayList<Object>();

		realAdd.add("净增用户（个）");

		for (int i = 1; i < newuser.size(); i++) {

			Integer newadd = Integer.parseInt(newuser.get(i).toString());

			Integer returnadd = Integer.parseInt(returnuser.get(i).toString());

			Integer real = newadd - returnadd;

			realAdd.add(real);
		}
		// 订购转化率（%）
		List<Object> loseRate = new ArrayList<Object>();

		loseRate.add("订购转化率（%）");

		for (int i = 1; i < online.size(); i++) {

			BigDecimal b1 = new BigDecimal(newuser.get(i).toString());

			BigDecimal b2 = new BigDecimal(reachuser.get(i).toString());

			

			Double d3 = 0.0000;

			if (b2.doubleValue() != 0) {

				d3 = b1.divide(b2, 6, BigDecimal.ROUND_HALF_UP).doubleValue();
			}

			NumberFormat nf = NumberFormat.getPercentInstance();

			nf.setMaximumFractionDigits(2);

			loseRate.add(nf.format(d3));
		}
		// 存留率
		List<Object> retentionRate = new ArrayList<Object>();

		retentionRate.add("留存率（%）");

		for (int i = 1; i < realAdd.size(); i++) {

			BigDecimal b1 = new BigDecimal(returnIn3user.get(i).toString());

			BigDecimal b2 = new BigDecimal(newuser.get(i).toString());
			BigDecimal b3 =b2.subtract(b1);
			double d3 = 0.0000;

			if (b3.doubleValue() != 0 && b2.doubleValue()!=0) {

				d3 = b3.divide(b2, 6, BigDecimal.ROUND_HALF_UP).doubleValue();
			}

			NumberFormat nf = NumberFormat.getPercentInstance();

			nf.setMaximumFractionDigits(2);

			retentionRate.add(nf.format(d3));
		}

		if (indexs.contains(1)) {

			lists.add(online);
		}
		if (indexs.contains(2)) {

			lists.add(reachuser);
		}
		if (indexs.contains(3)){

			lists.add(newuser);
		}
		if (indexs.contains(4)) {

			lists.add(returnuser);
		}

		if (indexs.contains(5)) {

			lists.add(returnIn3user);
		}

		if (indexs.contains(6)) {

			lists.add(realAdd);
		}

		if (indexs.contains(7)) {

			lists.add(loseRate);
		}

		if (indexs.contains(8)) {

			lists.add(retentionRate);
		}

		
		return lists;
	}
	
	
	
	/*
	 * 计算产品列
	 * */
	@SuppressWarnings("unchecked")
	public List getLie() {
		// 全部集合
		List lists = new ArrayList();

		StringBuffer sbf = new StringBuffer();
		sbf.append(" SELECT DISTINCT A.T_CHANNEL_NAME  ");
		sbf.append("FROM WX.C_ONLINEUSER_CHANNEL A");
		sbf.append("  ORDER BY A.T_CHANNEL_NAME ");
		String sql = sbf.toString();

		List listsome = new ArrayList();

		listsome.add("渠道");

		try {
			List listss = dao.findBySql(sql, null);
			if (listss != null) {
				for (int i = 0; i < listss.size(); i++) {
					listsome.add(listss.get(i));
				}
				//listsome.add(listss);
			}
		} catch (ServiceException e) {

			e.printStackTrace();
		}
		listsome.add("合计");
		return listsome;
	}

}
