$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	//获取用户输入的标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	//发送异步请求(POST)
	//三个参数：1.请求路径 2.要发送的数据 3.回调函数处理返回信息
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title, "content":content},
		function (data){
			//将返回结果转为JSON格式
			data = $.parseJSON(data);
			//在提示框中显示返回的信息
			$("#hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			//2秒后，自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//刷新页面
				if(data.code == 0){
					window.location.reload();
				}
			}, 2000);
		}
	);
}