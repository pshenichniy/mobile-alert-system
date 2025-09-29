const express = require('express');
const cors = require('cors');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, '../admin')));

// Простая "база данных" в памяти
let serverState = {
    actionEnabled: false,
    message: "🔔 Важное сообщение! Проверьте систему!",
    lastActivated: null,
    clients: []
};

// Логирование запросов
app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
    next();
});

// 📱 API для клиента - проверка статуса
app.get('/api/status', (req, res) => {
    const response = {
        hasAction: serverState.actionEnabled,
        message: serverState.message,
        timestamp: new Date().toISOString(),
        serverTime: Date.now()
    };

    console.log('📡 Статус запрошен:', response);
    res.json(response);
});

// 🎛️ API для админки - активация тревоги
app.post('/api/activate', (req, res) => {
    const { message, group } = req.body;

    serverState.actionEnabled = true;
    serverState.message = message || "🚨 Тревога! Проверьте систему!";
    serverState.lastActivated = new Date();

    console.log('🚨 ТРЕВОГА АКТИВИРОВАНА:', serverState.message);

    res.json({
        success: true,
        message: "Тревога активирована",
        data: serverState
    });
});

// 🔕 API для админки - деактивация
app.post('/api/deactivate', (req, res) => {
    serverState.actionEnabled = false;

    console.log('✅ Тревога деактивирована');

    res.json({
        success: true,
        message: "Тревога выключена",
        data: serverState
    });
});

// ℹ️ Информация о сервере
app.get('/api/info', (req, res) => {
    res.json({
        name: "Mobile Alert Server",
        version: "1.0.0",
        status: "running",
        uptime: process.uptime(),
        state: serverState
    });
});

// 🏠 Главная страница - перенаправляем на админку
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, '../admin/index.html'));
});

// Запуск сервера
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🎯 Сервер запущен на порту ${PORT}`);
    console.log(`📍 Локальный доступ: http://localhost:${PORT}`);
    console.log(`📱 API статуса: http://localhost:${PORT}/api/status`);
    console.log(`🎛️ Админ панель: http://localhost:${PORT}/`);
});

// Обработка graceful shutdown
process.on('SIGINT', () => {
    console.log('\n🛑 Выключение сервера...');
    process.exit(0);
});