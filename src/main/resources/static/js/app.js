document.addEventListener('DOMContentLoaded', function () {
    const toggleButton = document.querySelector('[data-password-toggle]');
    const passwordInput = document.querySelector('#password');

    if (toggleButton && passwordInput) {
        toggleButton.addEventListener('click', function () {
            const isHidden = passwordInput.type === 'password';
            passwordInput.type = isHidden ? 'text' : 'password';
            toggleButton.textContent = isHidden ? 'Ocultar' : 'Mostrar';
        });
    }
});
