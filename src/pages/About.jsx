import React, { useState, useEffect } from 'react';
import SEO from '../components/SEO';

const WORDS = ['Vývojář', 'Nadšenec do AI', 'Týmový hráč', 'Věčný student'];

const About = () => {
    // --- Logika pro Typewriter efekt (čistý JS/React) ---
    const [text, setText] = useState('');
    const [isDeleting, setIsDeleting] = useState(false);
    const [loopNum, setLoopNum] = useState(0);
    const [typingSpeed, setTypingSpeed] = useState(150);

    useEffect(() => {
        const handleType = () => {
            const i = loopNum % WORDS.length;
            const fullText = WORDS[i];

            setText(isDeleting
                ? fullText.substring(0, text.length - 1)
                : fullText.substring(0, text.length + 1)
            );

            // Rychlost psaní vs mazání
            setTypingSpeed(isDeleting ? 100 : 150);

            if (!isDeleting && text === fullText) {
                // Slovo je dopsáno, čeká chvíli, pak začne mazat
                setTimeout(() => setIsDeleting(true), 2000);
            } else if (isDeleting && text === '') {
                // Slovo je smazáno, jde na další
                setIsDeleting(false);
                setLoopNum(prev => prev + 1);
            }
        };

        const timer = setTimeout(handleType, typingSpeed);

        return () => clearTimeout(timer);
    }, [text, isDeleting, loopNum, typingSpeed]); // Závislosti efektu

    // --- Renderování komponenty ---
    return (
        <>
            <SEO
                title="O nás"
                description="Poznejte příběh projektu SecondEl. Férový nákup a prodej prověřené elektroniky se zárukou a důrazem na udržitelnost."
                canonical="https://secondel.cz/about"
            />
            <style>{`
                @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800;900&display=swap');

                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                    font-family: 'Poppins', sans-serif;
                }

                body {
                    background-color: #000;
                    width: 100%;
                    overflow-x: hidden;
                }

                /* Layout Grid (vlastní náhrada za Bootstrap) */
                .container-fluid {
                    width: 100%;
                    padding-right: 15px;
                    padding-left: 15px;
                    margin-right: auto;
                    margin-left: auto;
                }
                .row {
                    display: flex;
                    flex-wrap: wrap;
                    margin-right: -15px;
                    margin-left: -15px;
                }
                .col-12 {
                    flex: 0 0 100%;
                    max-width: 100%;
                    padding: 0 15px;
                }
                @media (min-width: 992px) {
                    .col-lg-6 {
                        flex: 0 0 50%;
                        max-width: 50%;
                        padding: 0 15px;
                    }
                }

                /* Typografie a barvy */
                .text-brand-orange { color: #f4623a !important; }
                .text-brand-teal { color: #20c997 !important; }
                .text-brand-yellow { color: #ffc107 !important; }
                .text-brand-cyan { color: #0dcaf0 !important; }

                .display-2 {
                    font-size: clamp(2.5rem, 5vw, 4.5rem);
                    font-weight: 700;
                    line-height: 1.1;
                }
                .lead {
                    font-size: 1.15rem;
                    font-weight: 300;
                    line-height: 1.7;
                }

                /* Specifické sekce */
                .portfolio-header {
                    min-height: 85vh;
                    display: flex;
                    align-items: center;
                }

                .profile-img-container {
                    position: relative;
                    width: 280px;
                    height: 280px;
                    margin: 0 auto;
                }
                @media (min-width: 768px) {
                    .profile-img-container {
                        width: 380px;
                        height: 380px;
                    }
                }

                .profile-img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                    border-radius: 50%;
                    border: 4px solid #f4623a;
                    box-shadow: 0 0 35px rgba(244, 98, 58, 0.4);
                }

                /* Typewriter efekt */
                .typewrite > .wrap {
                    border-right: 0.08em solid #fff;
                    animation: blink 0.7s infinite;
                }
                @keyframes blink {
                    0%, 100% { border-color: transparent; }
                    50% { border-color: #fff; }
                }

                /* Karty */
                .custom-card {
                    background: #111;
                    border: 1px solid #222;
                    border-radius: 12px;
                    padding: 24px;
                    height: 100%;
                    transition: transform 0.3s ease, border-color 0.3s ease;
                }
                .custom-card:hover {
                    transform: translateY(-5px);
                    border-color: #444;
                }

                /* Responzivní video */
                .video-responsive {
                    overflow: hidden;
                    padding-bottom: 56.25%;
                    position: relative;
                    height: 0;
                    border-radius: 12px;
                    border: 1px solid #222;
                }
                .video-responsive iframe {
                    left: 0;
                    top: 0;
                    height: 100%;
                    width: 100%;
                    position: absolute;
                }

                /* Tlačítka */
                .btn-custom {
                    display: inline-block;
                    padding: 12px 28px;
                    border-radius: 30px;
                    font-weight: 500;
                    text-decoration: none;
                    transition: all 0.3s ease;
                    cursor: pointer;
                }
                .btn-solid-orange {
                    background-color: #f4623a;
                    color: #fff;
                    border: 2px solid #f4623a;
                }
                .btn-solid-orange:hover {
                    background-color: #d94d26;
                    border-color: #d94d26;
                }
                .btn-outline-white {
                    background-color: transparent;
                    color: #fff;
                    border: 2px solid #fff;
                }
                .btn-outline-white:hover {
                    background-color: #fff;
                    color: #000;
                }

                /* Utilities */
                .bg-dark-secondary { background-color: #0a0a0a; }
                .text-muted { color: #888 !important; }
                .badge {
                    display: inline-block;
                    padding: 4px 8px;
                    font-size: 0.75rem;
                    font-weight: 600;
                    border-radius: 4px;
                    text-transform: uppercase;
                }
            `}</style>

            <div style={{ backgroundColor: '#000', color: '#fff', minHeight: '100vh' }}>
                {/* --- 1. HERO SEKCIA --- */}
                <header className="portfolio-header">
                    <div className="container-fluid" style={{ maxWidth: '1200px' }}>
                        <div className="row align-items-center">
                            <div className="col-12 col-lg-6" style={{ marginBottom: '40px' }}>
                                <span className="badge text-brand-orange" style={{ border: '1px solid #f4623a', marginBottom: '15px' }}>
                                    Portfolio
                                </span>
                                <h1 className="display-2" style={{ marginBottom: '15px' }}>
                                    Jan Macner
                                </h1>
                                <div style={{ fontSize: 'clamp(1.2rem, 3vw, 1.8rem)', minHeight: '45px', marginBottom: '20px', fontWeight: 300 }}>
                                    Jsem{' '}
                                    <span className="typewrite text-brand-orange" style={{ fontWeight: 600 }}>
                                        <span className="wrap">{text}</span>
                                    </span>
                                </div>
                                <p className="lead text-muted" style={{ marginBottom: '30px', maxWidth: '500px' }}>
                                    Vítejte na mém webu. Tady najdete přehled mých projektů, dovedností a technologií, se kterými pracuji. Můžete se podívat na to, co mě baví a čemu se věnuji.
                                </p>
                                <div style={{ display: 'flex', gap: '15px', flexWrap: 'wrap' }}>
                                    <a href="#about" className="btn-custom btn-solid-orange">Více o mně</a>
                                    <a href="#contact" className="btn-custom btn-outline-white">Kontakt</a>
                                </div>
                            </div>
                            <div className="col-12 col-lg-6" style={{ textAlign: 'center' }}>
                                <div className="profile-img-container">
                                    <img
                                        src="/uploads/20251121_211246.jpg"
                                        alt="profilovka"
                                        className="profile-img"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                </header>

                {/* --- 2. O MNĚ --- */}
                <section id="about" style={{ padding: '80px 0', borderTop: '1px solid #1a1a1a' }} className="bg-dark-secondary">
                    <div className="container-fluid" style={{ maxWidth: '1200px' }}>
                        <div className="row">
                            <div className="col-12 col-lg-6" style={{ marginBottom: '40px' }}>
                                <h2 style={{ fontSize: '2rem', fontWeight: 600, marginBottom: '20px' }}>O mně</h2>
                                <p className="text-muted" style={{ lineHeight: '1.8', marginBottom: '20px' }}>
                                    Věnuji se vývoji webových aplikací s důrazem na čistý kód, moderní architekturu a uživatelský zážitek.
                                    Mám zkušenosti s frontendem i backendem, rád propojuji technologie a hledám funkční řešení pro reálné problémy.
                                </p>
                                <p className="text-muted" style={{ lineHeight: '1.8' }}>
                                    Stále se učím nové věci, zajímám se o integraci umělé inteligence do běžného vývoje a baví mě pracovat
                                    v týmu lidí, kteří se navzájem posouvají kupředu.
                                </p>
                            </div>
                            <div className="col-12 col-lg-6">
                                <div className="row" style={{ rowGap: '20px' }}>
                                    <div className="col-12" style={{ flex: '0 0 50%', maxWidth: '50%' }}>
                                        <div className="custom-card">
                                            <h3 className="text-brand-orange" style={{ fontSize: '2rem', fontWeight: 700, margin: '0 0 5px 0' }}>Java</h3>
                                            <p className="text-muted" style={{ margin: 0, fontSize: '0.9rem' }}>Backend vývoj & Spring</p>
                                        </div>
                                    </div>
                                    <div className="col-12" style={{ flex: '0 0 50%', maxWidth: '50%' }}>
                                        <div className="custom-card">
                                            <h3 className="text-brand-teal" style={{ fontSize: '2rem', fontWeight: 700, margin: '0 0 5px 0' }}>React</h3>
                                            <p className="text-muted" style={{ margin: 0, fontSize: '0.9rem' }}>Moderní frontend</p>
                                        </div>
                                    </div>
                                    <div className="col-12" style={{ flex: '0 0 50%', maxWidth: '50%' }}>
                                        <div className="custom-card">
                                            <h3 className="text-brand-yellow" style={{ fontSize: '2rem', fontWeight: 700, margin: '0 0 5px 0' }}>Git</h3>
                                            <p className="text-muted" style={{ margin: 0, fontSize: '0.9rem' }}>Správa verzí & CI/CD</p>
                                        </div>
                                    </div>
                                    <div className="col-12" style={{ flex: '0 0 50%', maxWidth: '50%' }}>
                                        <div className="custom-card">
                                            <h3 className="text-brand-cyan" style={{ fontSize: '2rem', fontWeight: 700, margin: '0 0 5px 0' }}>AI</h3>
                                            <p className="text-muted" style={{ margin: 0, fontSize: '0.9rem' }}>Automatizace a modely</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                {/* --- 3. VIDEO SEKCIA --- */}
                <section style={{ padding: '80px 0', borderTop: '1px solid #1a1a1a' }}>
                    <div className="container-fluid" style={{ maxWidth: '900px', textAlign: 'center' }}>
                        <span className="badge text-brand-orange" style={{ border: '1px solid #f4623a', marginBottom: '15px' }}>
                            Ukázka
                        </span>
                        <h2 style={{ fontSize: '2rem', fontWeight: 600, marginBottom: '15px' }}>Moje práce v akci</h2>
                        <p className="text-muted" style={{ marginBottom: '40px', maxWidth: '600px', margin: '0 auto 40px auto' }}>
                            Krátké video představující ukázku mých projektů, vývojového prostředí nebo mého přístupu k řešení úkolů.
                        </p>
                        <div className="video-responsive">
                            <iframe
                                src="https://www.youtube.com/embed/dQw4w9WgXcQ"
                                title="Video ukázka"
                                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                                allowFullScreen
                            />
                        </div>
                    </div>
                </section>

                {/* --- 4. KONTAKT --- */}
                <footer id="contact" style={{ padding: '80px 0 40px 0', borderTop: '1px solid #1a1a1a' }} className="bg-dark-secondary">
                    <div className="container-fluid" style={{ maxWidth: '1200px' }}>
                        <div className="row align-items-center" style={{ marginBottom: '60px' }}>
                            <div className="col-12 col-lg-6" style={{ marginBottom: '30px' }}>
                                <h2 style={{ fontSize: '2rem', fontWeight: 600, marginBottom: '15px' }}>Máte zájem o spolupráci?</h2>
                                <p className="text-muted" style={{ margin: 0 }}>
                                    Neváhejte se mi ozvat, rád si popovídám o nových příležitostech, projektech nebo nápadech.
                                </p>
                            </div>
                            <div className="col-12 col-lg-6" style={{ textAlign: 'right' }}>
                                <a href="mailto:jan.macner@example.com" className="btn-custom btn-solid-orange">
                                    Napsat e-mail
                                </a>
                            </div>
                        </div>
                        <div style={{ borderTop: '1px solid #1a1a1a', paddingTop: '30px', textAlign: 'center' }}>
                            <p className="text-muted" style={{ margin: 0, fontSize: '0.85rem' }}>
                                &copy; {new Date().getFullYear()} Jan Macner. Všechna práva vyhrazena.
                            </p>
                        </div>
                    </div>
                </footer>
            </div>
        </>
    );
};

export default About;
