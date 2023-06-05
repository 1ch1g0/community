$(function (){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

//点赞
function like(btn, entityType, entityId, entityUserId, postId) {
    //异步请求
    $.post(
        //请求路径
        CONTEXT_PATH + "/like",
        //请求参数
        {"entityType":entityType, "entityId":entityId, "entityUserId":entityUserId, "postId":postId},
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

//置顶
function setTop(){
    var btn = this;
    if($(btn).hasClass("active")){
        $.post(
            CONTEXT_PATH + "/discuss/untop",
            {"id":$("#postId").val()},
            function (data){
                data = $.parseJSON(data);
                if(data .code == 0){
                    //$("#topBtn").attr("disabled", "disabled");
                    window.location.reload();
                }else{
                    alert(data.msg);
                }
            }
        );
    }else{
        $.post(
            CONTEXT_PATH + "/discuss/top",
            {"id":$("#postId").val()},
            function (data){
                data = $.parseJSON(data);
                if(data .code == 0){
                    //$("#topBtn").attr("disabled", "disabled");
                    window.location.reload();
                }else{
                    alert(data.msg);
                }
            }
        );
    }
}

//加精
function setWonderful(){
    var btn = this;
    if($(btn).hasClass("active")){
        $.post(
            CONTEXT_PATH + "/discuss/unwonderful",
            {"id":$("#postId").val()},
            function (data){
                data = $.parseJSON(data);
                if(data .code == 0){
                    //$("#wonderfulBtn").attr("disabled", "disabled");
                    window.location.reload();
                }else{
                    alert(data.msg);
                }
            }
        );
    }else{
        $.post(
            CONTEXT_PATH + "/discuss/wonderful",
            {"id":$("#postId").val()},
            function (data){
                data = $.parseJSON(data);
                if(data .code == 0){
                    //$("#wonderfulBtn").attr("disabled", "disabled");
                    window.location.reload();
                }else{
                    alert(data.msg);
                }
            }
        );
    }
}

//删除
function setDelete(){
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data);
            if(data .code == 0){
                location.href = CONTEXT_PATH + "/index";
            }else{
                alert(data.msg);
            }
        }
    );
}