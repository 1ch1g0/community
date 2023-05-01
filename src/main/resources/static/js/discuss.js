function like(btn, entityType, entityId) {
    //异步请求
    $.post(
        //请求路径
        CONTEXT_PATH + "/like",
        //请求参数
        {"entityType":entityType, "entityId":entityId},
        //回调函数
        function (data){
            //将传回的数据转成JSON格式
            data = $.parseJSON(data);
            //判断状态
            if(data.code == 0){
                //点赞成功，更新数据和状态
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?"已赞":"赞");
            }else{
                //点赞失败
                alert(data.msg);
            }
        }
    )
}