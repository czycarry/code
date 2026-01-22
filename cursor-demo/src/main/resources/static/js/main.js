// 主 JavaScript 文件

document.addEventListener('DOMContentLoaded', function() {
    console.log('Netty HTTP Server - 前端页面已加载');
    
    // 更新页面加载时间
    updatePageLoadTime();
    
    // 检查服务器状态
    checkServerStatus();
});

/**
 * 更新页面加载时间
 */
function updatePageLoadTime() {
    const loadTime = performance.now().toFixed(2);
    console.log(`页面加载时间: ${loadTime}ms`);
}

/**
 * 检查服务器状态
 */
async function checkServerStatus() {
    try {
        const response = await fetch('/api/data');
        if (response.ok) {
            console.log('服务器状态: 正常');
        }
    } catch (error) {
        console.error('服务器连接失败:', error);
    }
}

/**
 * 发送 API 请求
 */
async function sendApiRequest(url, method = 'GET', data = null) {
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        }
    };
    
    if (data && method !== 'GET') {
        options.body = JSON.stringify(data);
    }
    
    try {
        const response = await fetch(url, options);
        const text = await response.text();
        
        let result;
        try {
            result = JSON.parse(text);
        } catch (e) {
            result = text;
        }
        
        return {
            success: response.ok,
            status: response.status,
            data: result
        };
    } catch (error) {
        return {
            success: false,
            error: error.message
        };
    }
}

/**
 * 格式化 JSON 显示
 */
function formatJSON(obj) {
    return JSON.stringify(obj, null, 2);
}

/**
 * 显示响应结果
 */
function displayResponse(elementId, result, isError = false) {
    const element = document.getElementById(elementId);
    if (!element) return;
    
    element.className = 'response-box ' + (isError ? 'error' : 'success');
    
    if (result.error) {
        element.textContent = `错误: ${result.error}`;
    } else if (result.data) {
        element.textContent = formatJSON(result.data);
    } else {
        element.textContent = formatJSON(result);
    }
}

/**
 * 显示加载状态
 */
function setLoading(buttonId, isLoading) {
    const button = document.getElementById(buttonId);
    if (!button) return;
    
    if (isLoading) {
        button.disabled = true;
        button.innerHTML = button.textContent + '<span class="loading"></span>';
    } else {
        button.disabled = false;
        button.innerHTML = button.textContent.replace('<span class="loading"></span>', '');
    }
}
