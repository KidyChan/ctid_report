package com.ctid.business.online.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.ctid.business.online.service.OnlineService;
import com.ctid.business.quotas.service.quotasService;
import com.ctid.core.base.BaseAction;
import com.ctid.util.DateHandler;
import com.ctid.vo.Quotas;
/**
 * 在网用户产品总表
 * 
 * @author 杜文雅
 *
 */
@SuppressWarnings("serial")
@Action(value = "onlineAll", results = { @Result(name = "success", location = "/jsp/online/onlineall.jsp") })
public class OnlineAction extends BaseAction {
	Log logger = LogFactory.getLog(OnlineAction.class);
	private final long quotaReportId=1;
	private Date date;
	private Date start;
	private Date end;
	private int syear;
	private int smonth;
	private int eyear;
	private int emonth;
	private int period;
	private List<Integer> yearList;
	private List<Integer> monthList;
	private String html;
	private String title;
	private List<Integer> quotaz = new ArrayList<Integer>(); // 指标id
	private String quotasStr;//指标json
	private List<Quotas> quotasList;
	private List<Integer> list_quotas;
	
	@Resource
	private quotasService quotasServiceImpl;
	@Resource
	private OnlineService onlineServiceImpl;

	public String execute() throws Exception {

		initForm();

		this.html = doQuery();
		String data = "";
		if (!"".equals(this.html)) {
			data = "havedata";
		}
		ServletActionContext.getRequest().setAttribute("data", data);
		return "success";
	}

	/**
	 * 执行查询
	 * 
	 * @return 查询结果HTML
	 */
	private String doQuery() {

		switch (period) {
		case 1:
			this.title = buildSingleDayTitle(date);
			return onlineServiceImpl.getOnlineSingleDay(date, quotaz, period);
		case 2:
			this.title = buildMutiDayTitle(start, end);
			return onlineServiceImpl.getOnlineMutiDay(start, end, quotaz,
					period);
		case 3:
			this.title = buildMonthTitle(syear, smonth, eyear, emonth);
			return onlineServiceImpl.getOnlineMonth(syear, smonth, eyear,
					emonth, quotaz, period);
		default:
			return "";
		}

	}

	/**
	 * 初始化表单项
	 */
	private void initForm() {
		if(period <= 0) {
			period = 1;
		}
		Date today = new Date();
		if (this.date == null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			this.date = cal.getTime();
		}

		if (this.start == null) {
			this.start = today;
		}

		if (this.end == null) {
			this.end = today;
		}

		if (this.syear <= 0) {
			this.syear = DateHandler.GetDateYear(today);
		}

		if (this.smonth <= 0) {
			this.smonth = DateHandler.GetDateMonth(today);
		}

		if (this.eyear <= 0) {
			this.eyear = DateHandler.GetDateYear(today);
		}

		if (this.emonth <= 0) {
			this.emonth = DateHandler.GetDateMonth(today);
		}
		this.quotasList=this.quotasServiceImpl.getQuotas(quotaReportId);

		myDataInit();
		myDataToJson();
		initYearList();
		initMonthList();

	}
private void myDataInit(){
		
		list_quotas = new ArrayList<Integer>();
		
		//以下-1代表全选id，
		
		if(quotaz != null){
			
			Collections.sort(quotaz);
			
			if(quotaz.contains(-1) && quotaz.size() > 1){
				
				quotaz.remove(0);
			
			}
			if(quotaz.contains(-1) && quotaz.size() == 1){
				
				quotaz.remove(0);
				
				for(int i=0;i<quotasList.size();i++){
					
					quotaz.add((int) quotasList.get(i).getQuotaId());
				}
			}
			
			if(quotaz != null){
				for (Integer i : quotaz) {
					list_quotas.add(i);
				}
			}
		}
		
		if(quotaz == null||quotaz.size()<=0){
			list_quotas.add(-1);
			for(int i=0;i<quotasList.size();i++){		
				long l = quotasList.get(i).getQuotaId();
				Long lm = Long.valueOf(l);
				Integer m = lm.intValue();
				quotaz.add(m);
			}
		}
	}
	
	private void myDataToJson() {
		
		//json化微信粉丝渠道
		JSONArray categoryJson = new JSONArray();
		JSONObject cjoStart = new JSONObject();
		cjoStart.put("id", -1);
		cjoStart.put("text", "全选");
		cjoStart.put("desc", "");	
		categoryJson.add(cjoStart);
		for (int i = 0; i < quotasList.size(); i++) {

			JSONObject jsobj = new JSONObject();
			jsobj.put("id", quotasList.get(i).getQuotaId());
			jsobj.put("text", quotasList.get(i).getQuotaName());
			jsobj.put("desc", "");
			categoryJson.add(jsobj);
		}
				
				
		quotasStr = categoryJson.toString();
	}

	/**
	 * 初始化年份下拉列表
	 */
	private void initYearList() {
		int end = DateHandler.GetDateYear(new Date());
		int start = end - 1;

		this.yearList = new ArrayList<Integer>();
		for (int i = start; i <= end; i++) {
			this.yearList.add(i);
		}
	}

	/**
	 * 初始化月份下拉列表
	 */
	private void initMonthList() {
		this.monthList = new ArrayList<Integer>();
		for (int i = 1; i <= 12; i++) {
			this.monthList.add(i);
		}
	}

	/**
	 * 构造单日查询结果标题
	 * 
	 * @param date
	 *            查询日期
	 * @return 标题字符串
	 */
	private String buildSingleDayTitle(Date date) {
		Date end = DateHandler.GetAfterDay(date, 1);
		end = DateHandler.GetAfterSecond(end, -1);

		Date now = new Date();
		if (end.after(now)) {
			end = now;
		}
		return buildTitleString(date, end);
	}

	/**
	 * 构造多日查询结果标题
	 * 
	 * @param start
	 *            查询起始时间
	 * @param end
	 *            查询截止时间
	 * @return 标题字符串
	 */
	private String buildMutiDayTitle(Date start, Date end) {
		end = DateHandler.GetAfterDay(end, 1);
		end = DateHandler.GetAfterSecond(end, -1);

		Date now = new Date();
		if (end.after(now)) {
			end = now;
		}
		return buildTitleString(start, end);
	}

	/**
	 * 构造按月查询结果标题
	 * 
	 * @param startYear
	 *            查询起始年份
	 * @param startMonth
	 *            查询起始月份
	 * @param endYear
	 *            查询截止年份
	 * @param endMonth
	 *            查询截止月份
	 * @return 标题字符串
	 */
	private String buildMonthTitle(int startYear, int startMonth, int endYear,
			int endMonth) {
		Date start = DateHandler.GetDate(startYear, startMonth, 1);
		Date end = DateHandler.GetDate(endYear, endMonth, 1);
		end = DateHandler.GetAfterMonth(end, 1);
		end = DateHandler.GetAfterSecond(end, -1);

		Date now = new Date();
		if (end.after(now)) {
			end = now;
		}
		return buildTitleString(start, end);
	}

	/**
	 * 构造标题字符串
	 * 
	 * @param start
	 *            查询起始时间
	 * @param end
	 *            查询截止时间
	 */
	private String buildTitleString(Date start, Date end) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年M月d日 H时m分s秒");
		String strStart = formatter.format(start);
		String strEnd = formatter.format(end);
		return "统计日期: " + strStart + " 至 " + strEnd;
	}


	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public int getSyear() {
		return syear;
	}

	public void setSyear(int syear) {
		this.syear = syear;
	}

	public int getSmonth() {
		return smonth;
	}

	public void setSmonth(int smonth) {
		this.smonth = smonth;
	}

	public int getEyear() {
		return eyear;
	}

	public void setEyear(int eyear) {
		this.eyear = eyear;
	}

	public int getEmonth() {
		return emonth;
	}

	public void setEmonth(int emonth) {
		this.emonth = emonth;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public List<Integer> getYearList() {
		return yearList;
	}

	public void setYearList(List<Integer> yearList) {
		this.yearList = yearList;
	}

	public List<Integer> getMonthList() {
		return monthList;
	}

	public void setMonthList(List<Integer> monthList) {
		this.monthList = monthList;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}




	public quotasService getQuotasServiceImpl() {
		return quotasServiceImpl;
	}

	public OnlineService getOnlineServiceImpl() {
		return onlineServiceImpl;
	}

	public void setQuotasServiceImpl(quotasService quotasServiceImpl) {
		this.quotasServiceImpl = quotasServiceImpl;
	}

	public void setOnlineServiceImpl(OnlineService onlineServiceImpl) {
		this.onlineServiceImpl = onlineServiceImpl;
	}

	public long getQuotaReportId() {
		return quotaReportId;
	}

	public List<Integer> getQuotaz() {
		return quotaz;
	}

	public String getQuotasStr() {
		return quotasStr;
	}

	public List<Quotas> getQuotasList() {
		return quotasList;
	}

	public List<Integer> getList_quotas() {
		return list_quotas;
	}

	public void setQuotaz(List<Integer> quotaz) {
		this.quotaz = quotaz;
	}

	public void setQuotasStr(String quotasStr) {
		this.quotasStr = quotasStr;
	}

	public void setQuotasList(List<Quotas> quotasList) {
		this.quotasList = quotasList;
	}

	public void setList_quotas(List<Integer> list_quotas) {
		this.list_quotas = list_quotas;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	

	
}
