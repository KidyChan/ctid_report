package com.ctid.business.online.service;

import java.util.Date;
import java.util.List;

/**
 * 营销渠道总表服务层
 * @author 杜文雅
 * @date 2015-8-24
 */
public interface MarketingChannelService {


	
	/**
	 * 获取营销渠道总表单日查询统计结果
	 * @param date 查询日期
	 * @param indexs  指标
	 * @return 数据表格html字符串
	 */
	public String getMarketingChannelSingleDay(Date date, List<Integer> indexs);
	
/**
 * 获取营销渠道总表按日查询统计结果
 * @param start 查询起始日期
 * @param end 查询结束日期
 * @param indexs 指标
 * @return
 */
	public String getMarketingChannelMutiDay(Date start, Date end, List<Integer> indexs);
		
	/**
	 * 获取营销渠道总表单日查询统计结果
	 *  @param startYear 查询起始年份
	 * @param startMonth 查询起始月份
	 * @param endYear 查询截止年份
	 * @param endMonth 查询截止月份
	 * @param indexs 指标
	 * @return 数据表格HTML字符串
	 */
	public String getMarketingChannelMonth(int startYear, int startMonth, int endYear, int endMonth,List<Integer> indexs);

}

