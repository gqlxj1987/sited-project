!function(i,n){$.prototype.invalid=function(i){var n=$(this).find("[name='"+i+"']");n.addClass("error"),n.change(function(){n.removeClass("error"),n.off("change")})};var t={form:$("#user-reset-password-form"),submitBtn:$(".btn-submit"),init:function(){this.initEvent()},initEvent:function(){this.submitBtn.click(function(){for(var i=this.form.serializeArray(),n={},a=0;a<i.length;a+=1){var e=i[a];n[e.name]=e.value}if(!t.validate(n))return!1;t.submit(n)}.bind(this))},validate:function(i){return this.validateNewPassword(i.password,i.newPassword)},validateNewPassword:function(i,n){var t=i===n;return t||this.form.invalid("newPassword"),t},submit:function(n){$.ajax({url:"/web/api/user/reset-password/apply",method:"POST",data:JSON.stringify(n),contentType:"application/json",dataType:"json"}).then(function(){i.location.href="/login"}).fail(function(i){t.form.invalid(i.responseJSON.field)})}};t.init()}(this,this.document);