from flask import Flask, render_template_string, request, jsonify
import os

app = Flask(__name__)

# 存儲筆記的文本檔案
NOTES_FILE = "notes.txt"

# 極簡前端 HTML 模板
HTML_TEMPLATE = """
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Zen Pad | Vibe Coding</title>
    <style>
        body { background: #0d1117; color: #c9d1d9; font-family: 'Courier New', Courier, monospace; display: flex; flex-direction: column; align-items: center; min-height: 100vh; margin: 0; padding: 20px; }
        h1 { color: #58a6ff; font-weight: 200; letter-spacing: 5px; }
        #editor { width: 90%; max-width: 800px; height: 60vh; background: #161b22; border: 1px solid #30363d; border-radius: 8px; color: #e6edf3; padding: 20px; font-size: 1.1rem; outline: none; box-shadow: 0 4px 12px rgba(0,0,0,0.5); overflow-y: auto; }
        .status { margin-top: 15px; font-size: 0.8rem; color: #8b949e; }
    </style>
</head>
<body>
    <h1>ZEN PAD</h1>
    <div id="editor" contenteditable="true">載入中...</div>
    <div class="status" id="status">系統就緒</div>

    <script>
        const editor = document.getElementById('editor');
        const status = document.getElementById('status');

        // 從伺服器獲取初始內容
        fetch('/load').then(res => res.json()).then(data => {
            editor.innerText = data.content;
        });

        // 自動存檔邏輯 (Debounce)
        let timeout;
        editor.addEventListener('input', () => {
            status.innerText = '正在寫入心靈...';
            clearTimeout(timeout);
            timeout = setTimeout(() => {
                fetch('/save', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ content: editor.innerText })
                }).then(() => {
                    status.innerText = '思想已同步至硬碟';
                });
            }, 1000);
        });
    </script>
</body>
</html>
"""

@app.route('/')
def index():
    return render_template_string(HTML_TEMPLATE)

@app.route('/load', methods=['GET'])
def load():
    if os.path.exists(NOTES_FILE):
        with open(NOTES_FILE, "r", encoding="utf-8") as f:
            return jsonify({"content": f.read()})
    return jsonify({"content": "在這裡開始你的 Vibe Coding..."})

@app.route('/save', methods=['POST'])
def save():
    data = request.json
    with open(NOTES_FILE, "w", encoding="utf-8") as f:
        f.write(data.get("content", ""))
    return jsonify({"status": "success"})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
