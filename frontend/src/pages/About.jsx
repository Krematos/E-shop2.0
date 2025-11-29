import React, { useState, useEffect } from 'react';

const About = () => {
    // --- Logika pro Typewriter efekt (čistý JS/React) ---
    const [text, setText] = useState('');
    const [isDeleting, setIsDeleting] = useState(false);
    const [loopNum, setLoopNum] = useState(0);
    const [typingSpeed, setTypingSpeed] = useState(150);

    const words = ['Vývojář', 'Nadšenec do AI', 'Týmový hráč', 'Věčný student'];

    useEffect(() => {
        const handleType = () => {
            const i = loopNum % words.length;
            const fullText = words[i];

            setText(isDeleting
                ? fullText.substring(0, text.length - 1)
                : fullText.substring(0, text.length + 1)
            );

            // Rychlost psaní vs mazání
            setTypingSpeed(isDeleting ? 100 : 150);

            if (!isDeleting && text === fullText) {
                // Slovo je dopsáno, čekáme chvíli, pak začneme mazat
                setTimeout(() => setIsDeleting(true), 2000);
            } else if (isDeleting && text === '') {
                // Slovo je smazáno, jdeme na další
                setIsDeleting(false);
                setLoopNum(loopNum + 1);
            }
        };

        const timer = setTimeout(handleType, typingSpeed);

        return () => clearTimeout(timer);
    }, [text, isDeleting, loopNum]); // Závislosti efektu

    // --- Renderování komponenty ---
    return (
        <>
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
                    padding: 40px 10% 0 10%;
                    width: 100%;
                    max-width: 100%;
                }

                .row {
                    display: flex;
                    flex-wrap: wrap;
                    width: 100%;
                }

                .col-6 {
                    flex: 0 0 50%;
                    max-width: 50%;
                    padding: 0 15px;
                }

                /* Home Section */
                .home {
                    position: relative;
                    width: 100%;
                    min-height: 100vh;
                    padding: 100px 0 50px;
                    display: flex;
                    align-items: center;
                }

                .left h3 {
                    color: #fff;
                    font-size: 2.5rem;
                    font-weight: bold;
                    letter-spacing: 0.5px;
                }

                .left h1 {
                    color: #fff;
                    font-size: 4.5rem;
                    margin: 10px 0;
                    line-height: 1.1;
                }

                .left h4 {
                    color: #fff;
                    font-size: 2.2rem;
                    font-weight: bold;
                    margin-bottom: 20px;
                }

                .left p {
                    color: #fff;
                    font-size: 16px;
                    margin: 15px 0 20px 0;
                    width: 90%;
                    font-weight: 400;
                    line-height: 25px;
                    text-align: justify;
                }

                .left .btn {
                    display: flex;
                    align-items: center;
                    gap: 20px;
                    margin: 25px 0;
                }

                .left .btn button {
                    font-size: 15px;
                    font-weight: 600;
                    padding: 12px 24px;
                    border-radius: 25px;
                    background: #45b8ac;
                    border: 2px solid #45b8ac;
                    color: #000;
                    outline: none;
                    cursor: pointer;
                    transition: 0.3s ease-in;
                }

                .left .btn button:hover {
                    transform: translateY(-5px);
                    background: transparent;
                    color: #45b8ac;
                }

                .right {
                    display: flex;
                    justify-content: flex-end; /* Zarovnání doprava */
                    align-items: center;
                    height: 100%;
                }

                .right .profile {
                    position: relative;
                }

                .right .profile img {
                    width: 450px;
                    height: 450px;
                    object-fit: cover;
                    border-radius: 50%;
                    box-shadow: 0 0 40px rgba(7, 165, 37, 0.6);
                    border: 3px solid rgba(7, 165, 37, 1);
                }

                /* Text efekt */
                .multiple {
                    color: #bc243c;
                    text-shadow: 0 -1px 4px #FFF, 0 -2px 10px #ff0, 0 -10px 20px #ff8000, 0 -18px 40px #F00;
                }

                /* Blikající kurzor za textem */
                .cursor-blink {
                    display: inline-block;
                    margin-left: 2px;
                    width: 3px;
                    background-color: #bc243c;
                    animation: blink 1s infinite;
                }

                @keyframes blink {
                    0% { opacity: 0; }
                    50% { opacity: 1; }
                    100% { opacity: 0; }
                }

                /* Responsivita */
                @media screen and (max-width: 1280px) {
                    .container-fluid {
                        padding: 100px 50px;
                    }
                    .left h1 {
                        font-size: 3.5rem;
                    }
                    .right .profile img {
                        width: 350px;
                        height: 350px;
                    }
                }

                @media screen and (max-width: 992px) {
                    .col-6 {
                        flex: 0 0 100%;
                        max-width: 100%;
                    }
                    .home {
                        padding-top: 50px;
                        flex-direction: column;
                    }
                    .right {
                        justify-content: center;
                        margin-top: 50px;
                    }
                    .left {
                        text-align: center;
                    }
                    .left p {
                        width: 100%;
                        text-align: center;
                    }
                    .left .btn {
                        justify-content: center;
                    }
                }
            `}</style>

            <section className="home">
                <div className="container-fluid">
                    <div className="row">
                        <div className="col-6">
                            <div className="left">
                                <h3>Ahoj, jsem</h3>
                                <h1>Jan Macner</h1>
                                <h4>
                                    Jsem <span className="multiple">{text}</span>
                                    <span className="cursor-blink">&nbsp;</span>
                                </h4>
                                <p>
                                    Zdravím, jsem Jan Macner, nadšený vývojář se specializací na back-end.
                                    Umím programovat v Java, HTML, CSS a JavaScript. Umím používat SQL databáze.
                                    Ovládám také vizualizace v PowerBi a MS Office. Aktivně ve svých projektech
                                    využívám umělou inteligenci k zefektivnění práce. Rád se vzdělávám a učím novým věcem.
                                </p>
                                <div className="btn">
                                    <button>Stáhnout CV</button>
                                </div>
                            </div>
                        </div>
                        <div className="col-6">
                            <div className="right">
                                <div className="profile">
                                    <img src="/images/Já1.jpg" alt="profilovka" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </>
    );
};

export default About;