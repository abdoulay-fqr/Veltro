import Navbar from "@/components/landing/Navbar";
import Hero from "@/components/landing/Hero";
import WhyChoose from "@/components/landing/WhyChoose";
import GymAreas from "@/components/landing/GymAreas";
import Plans from "@/components/landing/Plans";
import Coaches from "@/components/landing/Coaches";
import Reviews from "@/components/landing/Reviews";
import ContactUs from "@/components/landing/ContactUs";
import Footer from "@/components/landing/Footer";

export default function Home() {
  return (
    <main>
      <Navbar />
      <Hero />
      <WhyChoose />
      <GymAreas />
      <Plans />
      <Coaches />
      <Reviews />
      <ContactUs />
      <Footer />
    </main>
  );
}