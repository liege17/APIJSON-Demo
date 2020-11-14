/*Copyright ©2016 TommyLemon(https://github.com/TommyLemon/APIJSON)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package apijson.jfinal;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.Controller;
import com.jfinal.server.undertow.UndertowServer;
import com.jfinal.template.Engine;

import apijson.Log;
import apijson.StringUtil;
import apijson.demo.DemoFunctionParser;
import apijson.demo.DemoParser;
import apijson.demo.DemoSQLConfig;
import apijson.demo.DemoSQLExecutor;
import apijson.demo.DemoVerifier;
import apijson.framework.APIJSONApplication;
import apijson.framework.APIJSONCreator;
import apijson.orm.FunctionParser;
import apijson.orm.Parser;
import apijson.orm.SQLConfig;
import apijson.orm.SQLExecutor;
import apijson.orm.Structure;
import apijson.orm.Verifier;


/**JFinalConfig
 * 右键这个类 > Run As > Java Application
 * @author Lemon
 */
public class AppRunnableConfig extends JFinalConfig {
	
	static {
		// APIJSON 配置 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		Map<String, Pattern> COMPILE_MAP = Structure.COMPILE_MAP;
		COMPILE_MAP.put("PHONE", StringUtil.PATTERN_PHONE);
		COMPILE_MAP.put("EMAIL", StringUtil.PATTERN_EMAIL);
		COMPILE_MAP.put("ID_CARD", StringUtil.PATTERN_ID_CARD);

		// 使用本项目的自定义处理类
		APIJSONApplication.DEFAULT_APIJSON_CREATOR = new APIJSONCreator() {

			@Override
			public Parser<Long> createParser() {
				return new DemoParser();
			}
			@Override
			public FunctionParser createFunctionParser() {
				return new DemoFunctionParser();
			}

			@Override
			public Verifier<Long> createVerifier() {
				return new DemoVerifier();
			}

			@Override
			public SQLConfig createSQLConfig() {
				return new DemoSQLConfig();
			}
			@Override
			public SQLExecutor createSQLExecutor() {
				return new DemoSQLExecutor();
			}

		};

		// APIJSON 配置 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	}


	public static void main(String[] args) throws Exception {
		UndertowServer.start(AppRunnableConfig.class);

		Log.DEBUG = true;  // 上线生产环境前改为 false，可不输出 APIJSONORM 的日志 以及 SQLException 的原始(敏感)信息
		APIJSONApplication.init();
	}


	// 支持 APIAuto 中 JavaScript 代码跨域请求 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	public void configRoute(Routes me) {
		me.add("/", DemoController.class);
	}

	public void configEngine(Engine me) {
		me.setBaseTemplatePath("webapp").setToClassPathSourceFactory();
	}

	public void configConstant(Constants me) {}
	public void configPlugin(Plugins me) {}
	public void configHandler(Handlers me) {}

	public void configInterceptor(Interceptors me) {
		me.add(new Interceptor() {

			@Override
			public void intercept(Invocation inv) {
				Controller controller = inv.getController();
				HttpServletRequest request = controller == null ? null : controller.getRequest();
				if (request == null) {
					return;
				}
				
				String origin = request.getHeader("origin");
		        String corsHeaders = request.getHeader("access-control-request-headers");
		        String corsMethod = request.getHeader("access-control-request-method");

		        HttpServletResponse response = controller.getResponse();
		        response.setHeader("Access-Control-Allow-Origin", StringUtil.isEmpty(origin, true) ? "*" : origin);
		        response.setHeader("Access-Control-Allow-Credentials", "true");
		        response.setHeader("Access-Control-Allow-Headers", StringUtil.isEmpty(corsHeaders, true) ? "*" : corsHeaders);
		        response.setHeader("Access-Control-Allow-Methods", StringUtil.isEmpty(corsMethod, true) ? "*" : corsMethod);
		        response.setHeader("Access-Control-Max-Age", "86400");

		        if("OPTIONS".equals(request.getMethod().toUpperCase())){
					controller.renderJson("{}");
					return;
				}
				
				inv.invoke();
			}
		});

	}

	// 支持 APIAuto 中 JavaScript 代码跨域请求 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

}
