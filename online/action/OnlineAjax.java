package com.ctid.business.online.action;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

import com.ctid.business.online.service.OnlineService;
import com.ctid.business.quotas.service.quotasService;
import com.ctid.core.base.AjaxBaseAction;
import com.ctid.vo.Quotas;

@Action(value = "OnlineAjax", results = { @Result(name = "success", type = "json") })
public class OnlineAjax extends AjaxBaseAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4566598698595570114L;
	private final long quotaReportId = 1;
	private String start;
	private String end;
	private String result;
	private String quotas;
	private int period;
	private int syear;
	private int smonth;
	private int eyear;
	private int emonth;
	@Resource
	private quotasService quotasServiceImpl;
	@Resource
	private OnlineService onlineServiceImpl;

	public String execute() {
		if(period <= 0) {
			period = 1;
		}
		Format f = new SimpleDateFormat("yyyy-MM-dd");
		List<Integer> quota = new ArrayList<Integer>();
		Date st = null;
		Date ed = null;
		
		try {
			st = (Date) f.parseObject(start);
			ed = (Date) f.parseObject(end);
		} catch (Exception e) {

		}
		if (quotas != null && quotas.length() > 0) {
			String[] array = quotas.split(",");

			if (array != null && array.length >= 1) {
				for (int i = 0; i < array.length; i++) {
					String str = array[i];
					quota.add(Integer.parseInt(str));
				}
			}
		}
		if (quota.contains(-1)) {
			quota.remove(0);
			if (quota.size() == 0 || quota == null) {
				List<Quotas> cate = this.quotasServiceImpl
						.getQuotas(quotaReportId);
				for (int i = 0; i < cate.size(); i++) {
					long l = cate.get(i).getQuotaId();
					Long lm = Long.valueOf(l);
					Integer m = lm.intValue();
					quota.add(m);
				}
			}
		}

		if(period==1){
			this.result = onlineServiceImpl.getOnlineSingleDay(st, quota,
					period);
		}
		if(period==2){
			this.result = onlineServiceImpl.getOnlineMutiDay(st, ed, quota,
					period);
		}
		if(period==3){
			this.result =onlineServiceImpl.getOnlineMonth(syear, smonth,eyear, emonth, quota, period);
		}
		return SUCCESS;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public long getQuotaReportId() {
		return quotaReportId;
	}

	public String getStart() {
		return start;
	}

	public String getEnd() {
		return end;
	}


	public String getResult() {
		return result;
	}

	public String getQuotas() {
		return quotas;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public void setQuotas(String quotas) {
		this.quotas = quotas;
	}

	public int getSyear() {
		return syear;
	}

	public int getSmonth() {
		return smonth;
	}

	public int getEyear() {
		return eyear;
	}

	public int getEmonth() {
		return emonth;
	}

	public void setSyear(int syear) {
		this.syear = syear;
	}

	public void setSmonth(int smonth) {
		this.smonth = smonth;
	}

	public void setEyear(int eyear) {
		this.eyear = eyear;
	}

	public void setEmonth(int emonth) {
		this.emonth = emonth;
	}
	
}
