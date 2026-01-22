// API 演示页面专用 JavaScript

/**
 * 测试数据接口
 */
async function testDataApi() {
    setLoading('testDataApi', true);
    displayResponse('data-response', { message: '正在请求...' });
    
    const result = await sendApiRequest('/api/data', 'GET');
    
    setLoading('testDataApi', false);
    displayResponse('data-response', result, !result.success);
}

/**
 * 测试回显接口
 */
async function testEchoApi() {
    const input = document.getElementById('echo-input');
    const content = input.value.trim();
    
    if (!content) {
        displayResponse('echo-response', { error: '请输入要回显的内容' }, true);
        return;
    }
    
    setLoading('testEchoApi', true);
    displayResponse('echo-response', { message: '正在发送...' });
    
    // 尝试解析为 JSON，如果不是 JSON 则作为普通文本发送
    let requestBody;
    try {
        requestBody = JSON.parse(content);
    } catch (e) {
        requestBody = content;
    }
    
    const result = await sendApiRequest('/api/echo', 'POST', requestBody);
    
    setLoading('testEchoApi', false);
    displayResponse('echo-response', result, !result.success);
}

/**
 * 测试自定义接口
 */
async function testCustomApi() {
    const url = document.getElementById('custom-url').value.trim();
    const method = document.getElementById('custom-method').value;
    const bodyText = document.getElementById('custom-body').value.trim();
    
    if (!url) {
        displayResponse('custom-response', { error: '请输入请求 URL' }, true);
        return;
    }
    
    setLoading('testCustomApi', true);
    displayResponse('custom-response', { message: '正在发送请求...' });
    
    let requestBody = null;
    if (bodyText) {
        try {
            requestBody = JSON.parse(bodyText);
        } catch (e) {
            displayResponse('custom-response', { error: '请求体不是有效的 JSON 格式' }, true);
            setLoading('testCustomApi', false);
            return;
        }
    }
    
    const result = await sendApiRequest(url, method, requestBody);
    
    setLoading('testCustomApi', false);
    displayResponse('custom-response', result, !result.success);
}

// 为按钮添加 ID（因为 onclick 中无法直接传递 this）
document.addEventListener('DOMContentLoaded', function() {
    // 为测试数据接口按钮添加 ID
    const dataBtn = document.querySelector('button[onclick="testDataApi()"]');
    if (dataBtn) {
        dataBtn.id = 'testDataApi';
    }
    
    // 为测试回显接口按钮添加 ID
    const echoBtn = document.querySelector('button[onclick="testEchoApi()"]');
    if (echoBtn) {
        echoBtn.id = 'testEchoApi';
    }
    
    // 为测试自定义接口按钮添加 ID
    const customBtn = document.querySelector('button[onclick="testCustomApi()"]');
    if (customBtn) {
        customBtn.id = 'testCustomApi';
    }
});
