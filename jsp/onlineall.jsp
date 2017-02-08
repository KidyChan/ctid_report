<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>产品在网用户分析产品总表</title>

<meta content="telephone=no" name="format-detection">
<link href="resource/styles/css.css" rel="stylesheet" type="text/css" />
<link href="resource/styles/exportwindow.css" rel="stylesheet"
	type="text/css" />
<link rel="stylesheet" type="text/css" href="resource/themes/easyui.css">
<link rel="stylesheet" type="text/css" href="resource/themes/icon.css">
<script src="resource/js/jquery.min.js"></script>
<script src="resource/js/jquery-1.11.2.js"></script>
<script src="resource/js/index.js"></script>
<script src="resource/js/menu_min.js"></script>
<script src="resource/js/printdiv.js"></script>
<script src="resource/js/showdate.js"></script>
<script src="resource/js/export.js"></script>
<script src="resource/js/jquery_date.js"></script>
<script type="text/javascript" src="resource/js/jquery.easyui.min.js"></script>
<script src="resource/js/tools.js"></script>
<style>
.navtworightbottom {
	margin-bottom: 20px;
}

#startDay {
	display: none;
}

#seMonth {
	display: none;
}

.navtbtn img {
	margin-left: 43%;
	margin-top: -1px;
	cursor: pointer;
}

#dataShow {
	width: 1117px;
	height: 86%;
	overflow-x: auto;
	overflow-y: auto;
}

table {
	height: auto;
	border-collapse: collapse;
	font-size: 12px;
	font-weight: normal;
	text-align: center;
	width: 1100px;
	border: 1px solid #4f8f98;
}

table th {
	border: 1px solid #4f8f98;
}

thead {
	background-color: rgb(228, 235, 242);
}
</style>
<script type="text/javascript">
	$(document).ready(function() {
		setShowDivHeight();
		/* 绑定伸缩按钮功能 */
		var searchDivH = $(".navtworighttop").height();
		
		/* 查询栏伸缩 */
		$(".navtbtn img").click(function(){
			var height = "";
			if($(".navtworighttop").css("display")=="none"){
				$(".navtworighttop").slideDown(300);
				$(".navtbtn img").attr("src","resource/images/shang.png");
				height = ($(".navtworightbottom").height()-searchDivH-20)+"px";
				$(".navtworightbottom").animate({"height":height},300);
			}else{
				$(".navtworighttop").slideUp(300);
				$(".navtbtn img").attr("src","resource/images/xia.png");
				height = ($(".navtworightbottom").height()+searchDivH+20)+"px";
				$(".navtworightbottom").animate({"height":height},300);
			}
		});
		
		$.ajax({
			/* url : "ajax/OnlineAjax.action", */
			 url : "http://localhost/interfaceutf.php",  
			type : "post",
			dataType : "json",
			data : {
				"start" : "2016-11-11"  ,   //new Date().Format("yyyy-MM-dd"),
				"end" :  "2016-11-11"  ,    //new Date().Format("yyyy-MM-dd"), 
				"quotas" : "-1",
				"period" : "2"
			},
			beforeSend : function() {
				$('#warning').empty();
				cleanTable();
				$(".ssspinner").show();
			}, 
			async : true,
			success : function(result) {
				$(".ssspinner").hide();
				var selectArray=getSelectArray(result.result);
				buildSelect("quotaz",selectArray,"quotasNameId","quotasName");  
				showTable(result.result);
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				alert('发生错误：' + XMLHttpRequest.responseText);
				alert(textStatus);
				alert(errorThrown);
			}
		});
		
		function getSelectArray(resultStr){
			if (resultStr != "") {
				resultStr = resultStr.substr(1,
						resultStr.length - 2);
				var resultJson = JSON.parse(resultStr);
				return resultJson.data.quotas;
			} else {
				console.log("没有数据");
				return;
			}
		}
		
		function cleanTable() {
			$("#dataShow #dataTable").empty();
			cleanfixedTable("dataTable","dataShow");
		}
		
		function showTable(json) {
			
			var period = $("input[name=period]:checked").attr("id");
			if(period=="period1"){
				var startTime = toDate($("#date").val()).Format("yyyy年MM月dd日 hh时mm分ss秒");
				var endTime = toDate($("#date").val()).Format("yyyy年MM月dd日");
			}
			
			if(period=="period2"){
				var startTime = toDate($("#startDate").val()).Format("yyyy年MM月dd日 hh时mm分ss秒");
				var endTime = toDate($("#endDate").val()).Format("yyyy年MM月dd日");
			}
			
			if (endTime == (new Date().Format("yyyy年MM月dd日"))) {
				endTime = new Date().Format("yyyy年MM月dd日 hh时mm分ss秒");
			}else{
				endTime = endTime + "23时59分59秒";
			}
			var resultStr = json;
			
			/* 更新表头时间信息 */
			$("#content #startTime").html(startTime);
			$("#content #endTime").html(endTime);
			
			if (resultStr != "") {
				resultStr = resultStr.substr(1,
						resultStr.length - 2);
				var resultJson = JSON.parse(resultStr);
				console.log(resultJson);
			} else {
				console.log("没有数据");
				return;
			}
			if (resultJson.total == 0) { 
				$('#warning').text("此期间没有数据，请选择其他日期。");
				return;
			}
			/*拼表格数据*/
			addTableHtml("dataTable", resultJson,period);
			fixedTable("dataTable","dataShow");
		}
		
		function addTableHtml(nodeID, json ,period) {
			var TrNums = json.data.product.length;
			var Days = json.total;
			var tableHead = $("<thead></thead>").appendTo($("#" + nodeID)); 
			/* 生成table头部 */
			if(period=="period2"){
				var fistHeadTr = $("<tr><td>日期</td></tr>").appendTo(tableHead);
				for(var i in json.data.list[0]){
					if(i!="proId"){
						var fistHeadTd = $("<td>"+i+"</td>");
						/* 得到日期栏需要合并的单元格数目 */
						var account = 0;
						for (var j in json.data.list[0][i]){
							account++;
						}
						fistHeadTd.attr("colspan",account);
						fistHeadTd.appendTo(fistHeadTr);
					}
				}
			}
			var secHeadTr = $("<tr><td>产品</td></tr>").appendTo(tableHead);
			for(var i=0;i<json.total;i++){
				for(var j in json.data.quotas){
					$("<td>"+json.data.quotas[j].quotasName+"</td>").appendTo(secHeadTr);
				}
			}
			/* 生成table数据部分 */
			var tableBody = $("<tbody></tbody>").appendTo($("#" + nodeID));
			for(var i=0;i<TrNums;i++){
				var tableTr = $("<tr><td>"+json.data.product[i].proName+"</td></tr>").appendTo(tableBody);
				var arrTr= new Array();
				for(var j in json.data.list[i]){
					for(var k in json.data.list[i][j]){
						arrTr.push(json.data.list[i][j][k]);
					}
				}
				for(var j in arrTr){
					$("<td>"+arrTr[j]+"</td>").appendTo(tableTr);
				}
			}
			
			/*生成合计部分*/
			/* var totalTr = $("<tr><td>合计</td></tr>").appendTo(tableBody);
			console.log(json.data.grandtotal);
			for(var i in json.data.grandtotal){
				
			} */
		}
		
		$("#btnQuery").click(function() {
			
			var singleDate = toDate($("#date").val());
			var myDate = new Date();
			var thisyear=myDate.getFullYear();
			var thismonth=myDate.getMonth();
			var startDate = toDate($("#startDate").val());
			var endDate = toDate($("#endDate").val());
			var date1=endDate.getTime()-startDate.getTime();
			var days=Math.floor(date1/(24*3600*1000));
			var startYear = $("#startYear").val();
			var startMonth = $("#startMonth").val();
			var endYear = $("#endYear").val();
			var endMonth = $("#endMonth").val();
			
			/* 获取下拉菜单中选中的渠道 */
			var selectValues = $("#quotaz").combobox(
			'getValues');
			var quotasStr = "";
			$.each(selectValues,function(i) {
								quotasStr += (((i == "0") ? ""
										: ",") + selectValues[i]);
							});
			var postData = {
					"start" : "",
					"end" : "",
					"quotas" : quotasStr,
					"period" : ""
			}
	
			
			switch ($("input[name=period]:checked").val()) {
			
			case "1":
				postData["period"] = "2"; /* 这里暂时调用的是2的接口，后期让邹永禄改一下单日的格式即可 */
				postData["start"] = $("#date").val();
				postData["end"] = $("#date").val();
				
				if(singleDate > myDate){
					
					alert("单日时间不能超过今天！");
					
					return false;
				}
				break;

			case "2":
				postData["period"] = "2";
				postData["start"] = $("#startDate").val();
				postData["end"] = $("#endDate").val();
				
				if(startDate > myDate||endDate> myDate){
					alert("查询时间不能超过今天！");
					return false;
				}else{
					if(startDate > endDate){
						
						alert("开始时间不能大于结束时间！");
						return false;
					}
					if(days>90){
						alert("时间差不能大于90日！");
						return false;
					}	
				}
				
				break;
				
			case "3":
				postData["period"] = "3";
				
				if(startYear>thisyear||endYear>thisyear){
					
					alert("	查询年份不能大于当前年份！");
					return false;
				}else{
					if(startYear > endYear){
						
						alert("开始年份不能大于结束年份！");
						return false;
					
					}else if(startYear == endYear && startMonth > endMonth){
							
						alert("开始月份不能大于结束月份！");
						return false;
					}
				}
				
				break;
			}
			
			/* ajax传值 */
			 $.ajax({
				 url : "ajax/OnlineAjax.action", 
				 /* url : "http://localhost/interfaceutf.php",   */
				type : "post",
				dataType : "json",
				data : {
					/* "start" : new Date().Format("yyyy-MM-dd"),
					"end" : new Date().Format("yyyy-MM-dd"), */
					"start" : "2016-10",
					"end" : "2016-11",
					"quotas" : "-1",
					"period" : 3
				},
				beforeSend : function() {
					$('#warning').empty();
					cleanTable();
					$(".ssspinner").show();
					
				}, 
				async : true,
				success : function(result) {
					$(".ssspinner").hide();
					showTable(result.result);
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					alert('发生错误：' + XMLHttpRequest.responseText);
					alert(textStatus);
					alert(errorThrown);
				}
			}); 
		});

	})

	$(document).ready(
			function() {
				// $(".nav .this").menu_tyk_q();
				$(".nav .this .er").show();
				$(".nav .this .h2").click(function() {
					if ($(this).siblings('.er').is(":hidden")) {
						$(this).siblings('.er').slideDown(300);
					} else {
						$(this).siblings('.er').slideUp(300);
					}
					;
					//$(this).children('.er').toggle("slow");	 
				});

				$(".nav .listBox .list:eq(1)").show();
				$(".nav .nav_top_right").addClass('hover').siblings()
						.removeClass('hover');

				$(".nav .listBox .list:eq(1)").show();

				$(".nav .nav_top_left")
						.click(
								function() {
									$(this).addClass('hover').siblings()
											.removeClass('hover');
									$(
											".nav .listBox .list:eq("
													+ $(this).index() + ")")
											.show().siblings().hide();
								});
				$(".nav .nav_top_right")
						.click(
								function() {
									$(this).addClass('hover').siblings()
											.removeClass('hover');
									$(
											".nav .listBox .list:eq("
													+ $(this).index() + ")")
											.show().siblings().hide();
								});

				$('.erji[onurl]').addClass('left_shu');
				$($('.erji[onurl]').attr('url')).show();
				$('.er .erji').click(
						function() {
							var upclass = $('.erji[onurl]').attr('url');
							$('.erji[onurl]').removeAttr('onurl').removeClass(
									'left_shu');
							$(upclass).hide();

							$(this).attr('onurl', 1).addClass('left_shu');
							$($(this).attr('url')).show();
						});

			});
	$().ready(function() {

		var browser_width = $(document.body).width();
		$("#rightDiv").css("height", $("div.nav").height());
		$("#rightDiv").css("width", browser_width);
	});
	window.onresize = function() {
		var browser_width = $(document.body).width();
		$("#rightDiv").css("height", $("div.nav").height());

		$("#rightDiv").css("width", browser_width);
		//alert(browser_width);
	}

	Date.prototype.Format = function(fmt) { //author: meizz 
		var o = {
			"M+" : this.getMonth() + 1, //月份 
			"d+" : this.getDate(), //日 
			"h+" : this.getHours(), //小时 
			"m+" : this.getMinutes(), //分 
			"s+" : this.getSeconds(), //秒 
			"q+" : Math.floor((this.getMonth() + 3) / 3), //季度 
			"S" : this.getMilliseconds()
		//毫秒 
		};
		if (/(y+)/.test(fmt))
			fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "")
					.substr(4 - RegExp.$1.length));
		for ( var k in o)
			if (new RegExp("(" + k + ")").test(fmt))
				fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k])
						: (("00" + o[k]).substr(("" + o[k]).length)));
		return fmt;
	}
	function resetData() {
		//设置查询周期为单日
		var radios = document.getElementsByName("period");
		for ( var i = 0; i < radios.length; i++) {
			radios[i].checked = false;
		}
		radios[0].checked = "checked";
		$("#com_quotas").combobox('setValues',"0");

		$("#oneAndMDay").show();
		$("#startDay").hide();
		$("#endDay").hide();
		$("#seMonth").hide();
		$("#oneDay").show();

		//设置时间都是当天
		//设置时间都是当天
		var defaultDate = '<s:date name="date" format="yyyy-MM-dd" />';
	 	var defaultYear = '<s:date name="date" format="yyyy" />';
		var defaultMonth = '<s:date name="date" format="M" />';
		
		$("#date").val(defaultDate);
		$("#startDate").val(defaultDate);
		$("#endDate").val(defaultDate);
		$("#startYear").val(defaultYear);
		$("#endYear").val(defaultYear);
		$("#startMonth").val(defaultMonth);
		$("#endMonth").val(defaultMonth);
		
	}




	
	$(document).ready(function() {
	/* 
	************* 根据查询周期显示不同的样式，如选中单日，则隐藏多日和按月的时间选择框！ 强力分隔符    *****************
	*
	*/
		selectRadio($("input[name=period]:checked").attr("id"));

		$("input[name=period]").change(function() {

			selectRadio($("input[name=period]:checked").attr("id"));

		});
				
		function selectRadio(radioId) {

			switch (radioId) {
				case "period1":

					$("#oneAndMDay").show();
					$("#startDay").hide();
					$("#endDay").hide();
					$("#seMonth").hide();
					$("#oneDay").show();
					break;
					
				case "period2":

					$("#oneAndMDay").show();
					$("#oneDay").hide();
					$("#seMonth").hide();
					$("#startDay").show();
					$("#endDay").show();

					break;
					
				case "period3":

					$("#oneAndMDay").hide();
					$("#seMonth").show();

					break;
			}
		}	
 })
  /************** 后台获取数据 装载easyui选择框 ****************/
  $(document).ready(function() {
	  

	   
  })
</script>

</head>
<body>
	<div class="right" id="rightDiv" style="top: 0px; position: absolute;">
		<div class="navtworighttop">
			<form action="onlineAll.action?quotaId=1" id="form2" method="post">
				<ul class="line">
					<li class="lineleft"><p>查询周期：</p></li>
					<li class="lineright">
						<div>
							<input type="radio" id="period1" name="period" value="1"
								<s:if test="period==1">checked="checked"</s:if> /> <label
								for="period1">单日</label>
						</div>
						<div>
							<input type="radio" id="period2" name="period" value="2"
								<s:if test="period==2">checked="checked"</s:if> /><label
								for="period2">多日</label>
						</div>
						<div>
							<input type="radio" id="period3" name="period" value="3"
								<s:if test="period==3">checked="checked"</s:if> /><label
								for="period3">按月</label>
						</div>
					</li>
					<div class="clear" style="clear: both;"></div>
				</ul>
				<div class="line" id="oneAndMDay">
					<ul class="data">
						<li id="oneDay">
							<p>单日时间：</p> <input type="text" readonly="readonly" id="date"
							name="date" value="<s:date name="date" format="yyyy-MM-dd" />"
							onClick="calendar.show({ id: this });" class="text_time1" />
						</li>
						<li id="startDay">
							<p>多日开始：</p> <input type="text" readonly="readonly"
							id="startDate" name="start"
							value="<s:date name="start" format="yyyy-MM-dd" />"
							onClick="calendar.show({ id: this });" class="text_time2" />
						</li>
						<li style="margin-left: 98px;" id="endDay">
							<p>多日结束：</p> <input type="text" readonly="readonly" id="endDate"
							name="end" value="<s:date name="end" format="yyyy-MM-dd" />"
							onClick="calendar.show({ id: this });" class="text_time1" />
						</li>
						<div class="clear" style="clear: both;"></div>
					</ul>
				</div>

				<div class="line" id="seMonth">
					<ul class="data">
						<li style="width: 324px;">
							<p>开始月份：</p>
							<div class="sel" style="margin-left: 0px;">
								<select id="startYear" name="syear" class="sele">
									<s:iterator id="yearList" value="yearList">
										<option value="<s:property value="#yearList" />"
											<s:if test="#yearList==syear">selected="selected"</s:if>>
											<s:property value="#yearList" /> 年
										</option>
									</s:iterator>
								</select>
							</div>
							<div class="selb">
								<select id="startMonth" name="smonth" class="sela">
									<s:iterator id="monthList" value="monthList">
										<option value="<s:property value="#monthList" />"
											<s:if test="#monthList==smonth">selected="selected"</s:if>>
											<s:property value="#monthList" /> 月
										</option>
									</s:iterator>
								</select>
							</div>
						</li>
						<li style="margin-left: 10px; width: 324px;">
							<p>结束月份：</p>
							<div class="sel">
								<select id="endYear" name="eyear" class="sele">
									<s:iterator id="yearList" value="yearList">
										<option value="<s:property value="#yearList" />"
											<s:if test="#yearList==eyear">selected="selected"</s:if>>
											<s:property value="#yearList" /> 年
										</option>
									</s:iterator>
								</select>
							</div>
							<div class="selb">
								<select id="endMonth" name="emonth" class="sela">
									<s:iterator id="monthList" value="monthList">
										<option value="<s:property value="#monthList" />"
											<s:if test="#monthList==emonth">selected="selected"</s:if>>
											<s:property value="#monthList" /> 月
										</option>
									</s:iterator>
								</select>
							</div>
						</li>
						<div class="clear" style="clear: both;"></div>
					</ul>
				</div>

				<ul class="line" style="padding-bottom: 0;">
					<li class="lineleft" style="float: left;">
						<p class="lileft">指&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp标：</p>
					</li>
					<li style="float: left;"><input class="easyui-combobox"
						id="quotaz" name="quotaz"
						data-options="
							method:'get',
							valueField:'id',
							textField:'text',
							multiple:true,
							width:'250',
							editable:false 
						">
					</li>
					<div class="clear" style="clear: both;"></div>
				</ul>

				<ul class="line">
					<li><input type="button" value="查&nbsp&nbsp询" class="xg_btn"
						id="btnQuery" /> <input onclick="resetData();" type="button"
						value="重&nbsp&nbsp置" class="xg_btnx" /> <input id="print"
						type="button" value="打&nbsp&nbsp印" class="xg_btnx" /> <input
						id="showbtn" type="button" value="导&nbsp&nbsp出" class="xg_btnx" />
					</li>
				</ul>

			</form>
		</div>
		<div class="navtbtn">
			<img src="resource/images/shang.png">
		</div>

		<div class="ssspinner">
			<div class="bounce1"></div>
			<div class="bounce2"></div>
			<div class="bounce3"></div>
			<p style="font-size: 22px; font-weight: bold; color: #666;">数据加载，请稍后</p>
		</div>
		<%if(request.getAttribute("data").equals("havedata")){%>
		<script type="text/javascript">
		  $('.navtworightbottom').show(0)
		  $('.ssspinner').hide(0);
		</script>

		<div class="navtworightbottom" id="content">
			<div class="rq" id="title">
				统计日期: <span id="startTime"></span> 至 <span id="endTime"></span>
			</div>
			<!-- 查询数据展示 -->
			<div id="dataShow">
				<p id="warning"></p>
				<div class="ssspinner">
					<div class="bounce1"></div>
					<div class="bounce2"></div>
					<div class="bounce3"></div>
					<p>数据加载，请稍候</p>
				</div>
				<table id="dataTable"></table>
			</div>
		</div>
		<%}%>
	</div>
	<!--第1页-->
	<div id="exportWindowHtml">
		<p class="showbtn">
			<a href="javascript:void(0);"></a>
		</p>
		<div id="bgExportWindow"></div>
		<div class="box" style="display: none">
			<div
				style="width: 336px; height: 174px; background: url(resource/images/which.png) no-repeat 18px 57px; border: 1px solid #cbcaca">
				<div id="topExportWindow">
					<p class="exportWindow"
						style="color: #fff; margin-left: 10px; line-height: 27px; float: left;">提示</p>
					<button class="exportWindow"
						style="width: 13px; height: 11px; float: right; margin-top: 5px; margin-right: 5px; background: #4780c8">
						<div class="close">
							<img src="resource/images/gb.png">
						</div>
					</button>
				</div>
				<p class="exportWindow"
					style="color: #555; font-size: 18px; margin-left: 107px; margin-top: 34px;">请选择您的导出方式</p>
				<div style="width: 336px; height: 27px; margin-top: 50px;">
					<button type="submit" class="exportWindow" id="exportWord"
						style="width: 101px; height: 27px; background: #6299e0; float: right; margin-right: 12px;">
						<p class="exportWindow"
							style="line-height: 27px; font-size: 14px; color: #fff; margin-left: 0px;">导出Word</p>
					</button>
					<button type="submit" class="exportWindow" id="exportExcel"
						style="width: 101px; height: 27px; background: #6299e0; float: right; margin-right: 12px;">
						<p class="exportWindow"
							style="line-height: 27px; font-size: 14px; color: #fff; margin-left: 0px;">导出Excel</p>
					</button>
				</div>
			</div>
		</div>
</body>
</html>