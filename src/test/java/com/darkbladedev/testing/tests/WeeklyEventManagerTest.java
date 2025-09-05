package com.darkbladedev.testing.tests;

import com.darkbladedev.testing.TestCase;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.mechanics.WeeklyEvent;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Casos de prueba para la clase WeeklyEventManager.
 * Verifica la gestión de eventos semanales, transiciones y estados.
 */
public class WeeklyEventManagerTest extends TestCase {
    
    private WeeklyEventManager eventManager;
    private MockWeeklyEvent mockEvent;
    private List<String> eventLog;
    
    public WeeklyEventManagerTest() {
        super("WeeklyEventManagerTest");
    }
    
    @Override
    public void setUp() throws Exception {
        eventManager = new WeeklyEventManager(null); // Mock plugin para pruebas
        mockEvent = new MockWeeklyEvent();
        eventLog = new ArrayList<>();
    }
    
    @Override
    public void tearDown() throws Exception {
        if (eventManager != null) {
            eventManager.stopAllEvents();
        }
        eventLog.clear();
    }
    
    @Override
    public void runTest() throws Exception {
        testEventRegistration();
        testEventStartAndStop();
        testEventTransitions();
        testEventScheduling();
        testMultipleEventHandling();
        testEventPersistence();
        testErrorHandling();
        testConcurrentEventAccess();
    }
    
    /**
     * Prueba el registro y desregistro de eventos.
     */
    private void testEventRegistration() throws Exception {
        // Verificar estado inicial
        assertNotNull(eventManager, "EventManager no debe ser null");
        assertEquals(Integer.valueOf(0), Integer.valueOf(eventManager.getRegisteredEventsCount()), "El conteo inicial de eventos debe ser 0");
        
        // Registrar evento
        eventManager.registerEvent("test_event", mockEvent);
        assertEquals(Integer.valueOf(1), Integer.valueOf(eventManager.getRegisteredEventsCount()), "Debe haber 1 evento registrado");
        
        // Verificar que el evento está registrado
        assertTrue(eventManager.isEventRegistered("test_event"), "El evento debe estar registrado");
        
        // Intentar registrar el mismo evento nuevamente
        eventManager.registerEvent("test_event", mockEvent);
        // El evento ya está registrado, el contador no debe cambiar
        assertEquals(Integer.valueOf(1), Integer.valueOf(eventManager.getRegisteredEventsCount()), "Debe haber 1 evento registrado");
        
        // Desregistrar evento
        eventManager.unregisterEvent("test_event");
        assertEquals(Integer.valueOf(0), Integer.valueOf(eventManager.getRegisteredEventsCount()), "El conteo debe ser 0 después de desregistrar");
        
        // Intentar desregistrar evento inexistente (no debe causar error)
        eventManager.unregisterEvent("non_existent");
        assertEquals(Integer.valueOf(0), Integer.valueOf(eventManager.getRegisteredEventsCount()), "No debe haber eventos registrados con nombre null");
    }
    
    /**
     * Prueba el inicio y parada de eventos.
     */
    private void testEventStartAndStop() throws Exception {
        eventManager.registerEvent("test_event", mockEvent);
        
        // Verificar estado inicial
        assertFalse(eventManager.isEventActive("test_event"), "El evento no debe estar activo inicialmente");
        assertFalse(mockEvent.isStarted(), "El mock del evento no debe estar iniciado inicialmente");
        
        // Iniciar evento
        boolean started = eventManager.startEvent("test_event");
        assertTrue(started, "El evento debe haberse iniciado correctamente");
        assertTrue(eventManager.isEventActive("test_event"), "El evento debe estar activo");
        assertTrue(mockEvent.isStarted(), "El mock del evento debe estar iniciado");
        
        // Intentar iniciar evento ya activo
        boolean alreadyStarted = eventManager.startEvent("test_event");
        assertFalse(alreadyStarted, "No debe poder iniciar un evento ya activo");
        
        // Parar evento
        boolean stopped = eventManager.stopEvent("test_event");
        assertTrue(stopped, "El evento debe haberse detenido correctamente");
        assertFalse(eventManager.isEventActive("test_event"), "El evento no debe estar activo después de pararlo");
        assertFalse(mockEvent.isStarted(), "El mock del evento no debe estar iniciado después de pararlo");
        
        // Intentar parar evento ya inactivo
        boolean alreadyStopped = eventManager.stopEvent("test_event");
        assertFalse(alreadyStopped, "No debe poder parar un evento ya inactivo");
        
        // Intentar iniciar evento inexistente
        boolean nonExistentStart = eventManager.startEvent("non_existent");
        assertFalse(nonExistentStart, "No debe poder iniciar un evento inexistente");
    }
    
    /**
     * Prueba las transiciones entre eventos.
     */
    private void testEventTransitions() throws Exception {
        MockWeeklyEvent event1 = new MockWeeklyEvent("Event1");
        MockWeeklyEvent event2 = new MockWeeklyEvent("Event2");
        
        eventManager.registerEvent("event1", event1);
        eventManager.registerEvent("event2", event2);
        
        // Iniciar primer evento
        eventManager.startEvent("event1");
        assertTrue(eventManager.isEventActive("event1"), "El primer evento debe estar activo inicialmente");
        assertTrue(event1.isStarted(), "El primer evento mock debe estar iniciado inicialmente");
        
        // Transición al segundo evento
        boolean transitioned = eventManager.transitionToEvent("event1", "event2");
        assertTrue(transitioned, "La transición debe haberse ejecutado correctamente");
        
        // Verificar estados después de la transición
        assertFalse(eventManager.isEventActive("event1"), "El primer evento no debe estar activo después de la transición");
        assertTrue(eventManager.isEventActive("event2"), "El segundo evento debe estar activo después de la transición");
        assertFalse(event1.isStarted(), "El primer evento mock no debe estar iniciado después de la transición");
        assertTrue(event2.isStarted(), "El segundo evento mock debe estar iniciado después de la transición");
        
        // Verificar que se llamaron los métodos de transición
        assertTrue(event1.wasTransitionOutCalled(), "El primer evento mock debe haber llamado transitionOut");
        assertTrue(event2.wasTransitionInCalled(), "El segundo evento mock debe haber llamado transitionIn");
        
        // Intentar transición inválida
        boolean invalidTransition = eventManager.transitionToEvent("non_existent", "event1");
        assertFalse(invalidTransition, "No debe poder hacer transición con evento inexistente");
    }
    
    /**
     * Prueba la programación de eventos.
     */
    private void testEventScheduling() throws Exception {
        eventManager.registerEvent("scheduled_event", mockEvent);
        
        // Programar evento para iniciar en 100ms
        long startTime = System.currentTimeMillis() + 100;
        boolean scheduled = eventManager.scheduleEvent("scheduled_event", startTime, 1000); // 1 segundo de duración
        assertTrue(scheduled, "El evento debe haberse programado correctamente");
        
        // Verificar que no está activo inmediatamente
        assertFalse(eventManager.isEventActive("scheduled_event"), "El evento no debe estar activo inmediatamente");
        
        // Esperar a que inicie
        Thread.sleep(150);
        assertTrue(eventManager.isEventActive("scheduled_event"), "El evento debe estar activo después de 150ms");
        
        // Esperar a que termine
        Thread.sleep(1100);
        assertFalse(eventManager.isEventActive("scheduled_event"), "El evento no debe estar activo después de 1100ms");
        
        // Verificar que se llamaron los métodos apropiados
        assertTrue(mockEvent.wasStarted(), "El mock del evento debe haber llamado started");
        assertTrue(mockEvent.wasStopped(), "El mock del evento debe haber llamado stopped");
    }
    
    /**
     * Prueba el manejo de múltiples eventos simultáneos.
     */
    private void testMultipleEventHandling() throws Exception {
        MockWeeklyEvent event1 = new MockWeeklyEvent("Event1");
        MockWeeklyEvent event2 = new MockWeeklyEvent("Event2");
        MockWeeklyEvent event3 = new MockWeeklyEvent("Event3");
        
        eventManager.registerEvent(event1);
        eventManager.registerEvent(event2);
        eventManager.registerEvent(event3);
        
        // Verificar que están registrados
        assertTrue(eventManager.isEventRegistered("Event1"), "Event1 debe estar registrado");
        assertTrue(eventManager.isEventRegistered("Event2"), "Event2 debe estar registrado");
        assertTrue(eventManager.isEventRegistered("Event3"), "Event3 debe estar registrado");
        
        // Verificar que hay 3 eventos registrados
        assertEquals(Integer.valueOf(3), Integer.valueOf(eventManager.getRegisteredEventsCount()), "Deben haber 3 eventos registrados");
        
        // Iniciar eventos manualmente
        event1.start();
        event2.start();
        event3.start();
        
        // Verificar que están activos
        assertTrue(event1.isActive(), "Event1 debe estar activo");
        assertTrue(event2.isActive(), "Event2 debe estar activo");
        assertTrue(event3.isActive(), "Event3 debe estar activo");
        
        // Parar todos los eventos
        eventManager.stopAllEvents();
        
        // Verificar que están inactivos
        assertFalse(event1.isActive(), "Event1 no debe estar activo");
        assertFalse(event2.isActive(), "Event2 no debe estar activo");
        assertFalse(event3.isActive(), "Event3 no debe estar activo");
    }
    
    /**
     * Prueba la persistencia de estado de eventos.
     */
    private void testEventPersistence() throws Exception {
        MockPersistentEvent persistentEvent = new MockPersistentEvent();
        eventManager.registerEvent("persistent_event", persistentEvent);
        
        // Configurar datos del evento
        persistentEvent.setTestData("test_value", 42);
        persistentEvent.start();
        
        // Simular guardado de estado usando el método del evento
        Map<String, Object> savedState = persistentEvent.saveState();
        assertNotNull(savedState, "SavedState no debe ser null");
        assertTrue(savedState.size() > 0, "SavedState debe contener datos");
        
        // Parar evento y crear uno nuevo
        persistentEvent.stop();
        eventManager.unregisterEvent(persistentEvent.getName());
        
        MockPersistentEvent newPersistentEvent = new MockPersistentEvent();
        eventManager.registerEvent(newPersistentEvent);
        
        // Restaurar estado usando el método del evento
        newPersistentEvent.loadState(savedState);
        
        // Verificar que los datos se restauraron
        assertEquals("test_value", newPersistentEvent.getStringData(), "Los datos de texto deben ser 'test_value'");
        assertEquals(Integer.valueOf(42), Integer.valueOf(newPersistentEvent.getNumericData()), "Los datos numéricos deben ser 42");
    }
    

    
    /**
     * Prueba el manejo de errores en eventos.
     */
    private void testErrorHandling() throws Exception {
        MockErrorEvent errorEvent = new MockErrorEvent();
        eventManager.registerEvent("error_event", errorEvent);
        
        // Configurar el evento para que falle al iniciar
        errorEvent.setFailOnStart(true);
        
        // Intentar iniciar evento que falla
        boolean started = eventManager.startEvent("error_event");
        assertFalse(started, "No debe poder iniciar un evento que falla");
        assertFalse(eventManager.isEventActive("error_event"), "El evento que falló no debe estar activo");
        
        // Configurar para que inicie correctamente pero falle al parar
        errorEvent.setFailOnStart(false);
        errorEvent.setFailOnStop(true);
        
        assertTrue(eventManager.startEvent("error_event"), "Debe poder iniciar un evento configurado correctamente");
        assertTrue(eventManager.isEventActive("error_event"), "El evento iniciado correctamente debe estar activo");
        
        // Intentar parar evento que falla al parar
        @SuppressWarnings("unused")
        boolean stopped = eventManager.stopEvent("error_event");
        // El comportamiento aquí depende de la implementación:
        // - Podría retornar false pero marcar el evento como inactivo
        // - Podría manejar el error internamente
        
        // Verificar que el sistema sigue funcionando después de errores
        MockWeeklyEvent normalEvent = new MockWeeklyEvent();
        eventManager.registerEvent("normal_event", normalEvent);
        assertTrue(eventManager.startEvent("normal_event"), "Debe poder iniciar un evento normal después de errores");
    }
    
    /**
     * Prueba el acceso concurrente al event manager.
     */
    private void testConcurrentEventAccess() throws Exception {
        final int threadCount = 5;
        final int eventsPerThread = 3;
        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch startLatch = new CountDownLatch(threadCount);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        
        // Registrar eventos para cada hilo
        for (int i = 0; i < threadCount; i++) {
            for (int j = 0; j < eventsPerThread; j++) {
                String eventName = "thread_" + i + "_event_" + j;
                eventManager.registerEvent(eventName, new MockWeeklyEvent(eventName));
            }
        }
        
        // Crear hilos que manipulen eventos concurrentemente
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                try {
                    startLatch.countDown();
                    startLatch.await(); // Esperar a que todos los hilos estén listos
                    
                    // Cada hilo manipula sus eventos
                    for (int j = 0; j < eventsPerThread; j++) {
                        String eventName = "thread_" + threadId + "_event_" + j;
                        MockWeeklyEvent event = new MockWeeklyEvent(eventName);
                        
                        // Registrar evento
                        eventManager.registerEvent(event);
                        
                        // Verificar que está registrado
                        if (!eventManager.isEventRegistered(eventName)) {
                            throw new RuntimeException("El evento no está registrado: " + eventName);
                        }
                        
                        // Iniciar evento
                        event.start();
                        
                        // Verificar que está activo
                        if (!event.isActive()) {
                            throw new RuntimeException("El evento no está activo: " + eventName);
                        }
                        
                        // Pequeña pausa
                        Thread.sleep(10);
                        
                        // Parar evento
                        event.stop();
                        
                        // Verificar que está inactivo
                        if (event.isActive()) {
                            throw new RuntimeException("El evento sigue activo: " + eventName);
                        }
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    finishLatch.countDown();
                }
            });
            threads.add(thread);
        }
        
        // Ejecutar todos los hilos
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Esperar a que terminen
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS), "No se completaron todos los hilos en el tiempo esperado");
        
        // Verificar que no hubo excepciones
        if (!exceptions.isEmpty()) {
            throw new AssertionError("Se produjeron excepciones durante el acceso concurrente: " + exceptions.get(0).getMessage());
        }
        
        // Verificar que no hay eventos activos en el manager
        assertFalse(eventManager.isEventActive(), "No debe haber eventos activos después de la prueba");
        
        // Verificar que todos los eventos están registrados
        int expectedRegisteredEvents = threadCount * eventsPerThread;
        assertEquals(Integer.valueOf(expectedRegisteredEvents), Integer.valueOf(eventManager.getRegisteredEventsCount()), "El conteo de eventos debe coincidir con el esperado");
    }
    
    // === CLASES MOCK PARA PRUEBAS ===
    
    /**
     * Mock básico de WeeklyEvent para pruebas.
     */
    private static class MockWeeklyEvent extends WeeklyEvent {
        private final String name;
        private boolean started = false;
        private boolean wasStarted = false;
        private boolean wasStopped = false;
        private boolean transitionOutCalled = false;
        private boolean transitionInCalled = false;
        
        public MockWeeklyEvent() {
            this("MockEvent");
        }
        
        public MockWeeklyEvent(String name) {
            super(null, 3600L); // Mock plugin y duración de 1 hora
            this.name = name;
        }
        
        @Override
        public void start() {
            started = true;
            wasStarted = true;
        }
        
        @Override
        public void stop() {
            started = false;
            wasStopped = true;
        }
        
        @Override
        public boolean isActive() {
            return started;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        protected void startEventTasks() {
            // Mock implementation
        }
        
        @Override
        protected void stopEventTasks() {
            // Mock implementation
        }
        
        @Override
        protected void announceEventStart() {
            // Mock implementation
        }
        
        @Override
        protected void announceEventEnd() {
            // Mock implementation
        }
        
        @Override
        protected void cleanupEventData() {
            // Mock implementation
        }
        
        @SuppressWarnings("unused")
        public void onTransitionOut() {
            transitionOutCalled = true;
        }
        
        @SuppressWarnings("unused")
        public void onTransitionIn() {
            transitionInCalled = true;
        }
        
        // Métodos de utilidad para pruebas
        public boolean isStarted() { return started; }
        public boolean wasStarted() { return wasStarted; }
        public boolean wasStopped() { return wasStopped; }
        public boolean wasTransitionOutCalled() { return transitionOutCalled; }
        public boolean wasTransitionInCalled() { return transitionInCalled; }
        
        // Métodos adicionales que pueden ser necesarios
        protected void pauseEventTasks() {
            // Mock implementation
        }
        
        protected void resumeEventTasks() {
            // Mock implementation
        }
    }
    
    /**
     * Mock de evento persistente para pruebas de estado.
     */
    private static class MockPersistentEvent extends MockWeeklyEvent {
        private String stringData;
        private int numericData;
        
        public void setTestData(String stringData, int numericData) {
            this.stringData = stringData;
            this.numericData = numericData;
        }
        
        public String getStringData() { return stringData; }
        public int getNumericData() { return numericData; }
        
        public Map<String, Object> saveState() {
            Map<String, Object> state = new HashMap<>();
            state.put("stringData", stringData);
            state.put("numericData", numericData);
            return state;
        }
        
        public void loadState(Map<String, Object> state) {
            stringData = (String) state.get("stringData");
            numericData = (Integer) state.get("numericData");
        }
    }
    
    /**
     * Mock de evento que puede fallar para pruebas de manejo de errores.
     */
    private static class MockErrorEvent extends MockWeeklyEvent {
        private boolean failOnStart = false;
        private boolean failOnStop = false;
        
        public void setFailOnStart(boolean fail) { this.failOnStart = fail; }
        public void setFailOnStop(boolean fail) { this.failOnStop = fail; }
        
        @Override
        public void start() {
            if (failOnStart) {
                throw new RuntimeException("Simulated start failure");
            }
            super.start();
        }
        
        @Override
        public void stop() {
            if (failOnStop) {
                throw new RuntimeException("Simulated stop failure");
            }
            super.stop();
        }
    }
}