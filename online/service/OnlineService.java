package com.ctid.business.online.service;

import java.util.Date;
import java.util.List;

/**
 * 在网产品总表服务层
 * @author 杜文雅
 * @date 2015-8-12
 */
public interface OnlineService {


/**
 * 获取在网总表按日查询统计结果
 * @param start 查询起始日期
 * @param end 查询结束日期
 * @param indexs 指标
 * @return
 */
	public String getOnlineMutiDay(Date start, Date end, List<Integer> indexs,int period);
		
	/**
	 * 获取在网总表单日查询统计结果
	 *  @param startYear 查询起始年份
	 * @param startMonth 查询起始月份
	 * @param endYear 查询截止年份
	 * @param endMonth 查询截止月份
	 * @param indexs 指标
	 * @return 数据表格HTML字符串
	 */
	public String getOnlineMonth(int startYear, int startMonth, int endYear, int endMonth,List<Integer> indexs,int period);
	/**
	 * 
	 * @return 当指标为0 时，只显示产品列表
	 */

}

